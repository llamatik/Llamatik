package com.llamatik.core.platform

import java.io.File
import java.util.Locale

/**
 * Picks and loads the desktop JNI library that fits the machine.
 *
 * Linux x86_64 ships several builds of the same JNI library (identical symbols, different ggml flags):
 *  - `llama_jni`                 AVX2 + FMA + F16C baseline (every CPU since Haswell / 2013)
 *  - `llama_jni_compat`          no AVX2/FMA/F16C, for older CPUs (a build for the default baseline would die with
 *                                SIGILL there and take the whole JVM with it)
 *  - `llama_jni_vulkan[_compat]` same two, with the Vulkan GPU backend (optional, only present when it was built)
 *
 * macOS and Windows only ship `llama_jni`.
 */
internal object DesktopNativeLoader {
    private const val BASE = "llama_jni"

    /** Instruction sets the default Linux x86_64 build requires. */
    private val baselineFlags = listOf("avx2", "fma", "f16c")

    internal fun platform(): String {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        return when {
            os.contains("mac") -> "macos"
            os.contains("linux") -> "linux"
            os.contains("win") -> "windows"
            else -> error("Unsupported OS: $os")
        }
    }

    /** CPU flags the default build needs but [cpuinfo] (`/proc/cpuinfo` text) does not list. Empty = fine/unknown. */
    internal fun missingBaseline(cpuinfo: String?, platform: String, arch: String): List<String> {
        if (platform != "linux" || (arch != "amd64" && arch != "x86_64")) return emptyList()
        val flags = cpuinfo?.lineSequence()
            ?.firstOrNull { it.startsWith("flags") }
            ?.substringAfter(':')?.trim()?.split(Regex("\\s+"))?.toSet()
            ?: return emptyList()
        return baselineFlags.filterNot { it in flags }
    }

    /**
     * Library base names to try, best first.
     * @param vulkan whether GPU variants may be tried at all (they fall back by themselves when libvulkan is absent)
     */
    internal fun candidates(platform: String, arch: String, cpuinfo: String?, vulkan: Boolean): List<String> {
        if (platform != "linux" || (arch != "amd64" && arch != "x86_64")) return listOf(BASE)
        val old = missingBaseline(cpuinfo, platform, arch).isNotEmpty()
        val cpu = if (old) "${BASE}_compat" else BASE
        return buildList {
            if (vulkan) add(if (old) "${BASE}_vulkan_compat" else "${BASE}_vulkan")
            add(cpu)
        }
    }

    private fun vulkanAllowed(): Boolean {
        val v = System.getProperty("llamatik.native.vulkan") ?: System.getenv("LLAMATIK_NATIVE_VULKAN")
        return v == null || !v.equals("false", ignoreCase = true)
    }

    private fun readCpuInfo(): String? =
        runCatching { File("/proc/cpuinfo").takeIf { it.canRead() }?.readText() }.getOrNull()

    /**
     * Extracts and loads the best JNI library. With [loadCompanions], every other library listed in
     * `native-libs.txt` is loaded first (shared ggml/llama libs on platforms that ship them).
     * @return the base name that was loaded, e.g. `llama_jni_compat`
     */
    fun loadJni(loadCompanions: Boolean = false): String {
        val platform = platform()
        val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)
        val cpuinfo = readCpuInfo()
        val resourceRoot = "/native/$platform"
        val tempDir = kotlin.io.path.createTempDirectory(prefix = "llamatik_${platform}_").toFile().also { it.deleteOnExit() }

        val listed = nativeResourceNames(platform)
        val companions = if (loadCompanions) listed.filterNot { it.contains(BASE) }.map { extract(resourceRoot, it, tempDir) } else emptyList()
        companions.forEach { System.load(it.absolutePath) }

        val missing = missingBaseline(cpuinfo, platform, arch)
        val tried = candidates(platform, arch, cpuinfo, vulkanAllowed())
            .filter { hasResource("$resourceRoot/${System.mapLibraryName(it)}") }

        if (missing.isNotEmpty() && tried.none { it.endsWith("_compat") }) {
            throw UnsatisfiedLinkError(
                "This CPU does not support ${missing.joinToString(", ") { it.uppercase() }}, which the bundled " +
                    "llama.cpp build requires, and this Llamatik build has no compatible native library " +
                    "(llama_jni_compat). Loading the default one would crash the JVM.",
            )
        }
        var lastError: Throwable? = null
        for (name in tried) {
            try {
                System.load(extract(resourceRoot, System.mapLibraryName(name), tempDir).absolutePath)
                return name
            } catch (e: UnsatisfiedLinkError) {
                // e.g. the Vulkan build without libvulkan.so.1 installed: try the next candidate
                lastError = e
            }
        }
        throw UnsatisfiedLinkError(
            "No native library could be loaded for $platform/$arch (tried ${tried.ifEmpty { listOf(BASE) }}): ${lastError?.message ?: "not found in resources"}",
        ).also { err -> lastError?.let { err.initCause(it) } }
    }

    private fun hasResource(path: String) = DesktopNativeLoader::class.java.getResource(path) != null

    private fun nativeResourceNames(platform: String): List<String> =
        DesktopNativeLoader::class.java.getResourceAsStream("/native/$platform/native-libs.txt")
            ?.bufferedReader()
            ?.useLines { lines -> lines.map(String::trim).filter { it.isNotEmpty() && !it.startsWith("#") }.toList() }
            .orEmpty()

    private fun extract(resourceRoot: String, resourceName: String, tempDir: File): File {
        val path = "$resourceRoot/$resourceName"
        val input = DesktopNativeLoader::class.java.getResourceAsStream(path)
            ?: error("Native library not found in resources: $path")
        val out = File(tempDir, resourceName)
        input.use { i -> out.outputStream().use { o -> i.copyTo(o) } }
        out.deleteOnExit()
        return out
    }
}
