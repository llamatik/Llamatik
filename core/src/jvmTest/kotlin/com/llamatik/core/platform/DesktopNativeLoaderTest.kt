package com.llamatik.core.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopNativeLoaderTest {
    private val sandyBridge = "processor : 0\nflags\t\t: fpu sse4_2 avx aes xsave\n"
    private val haswell = "processor : 0\nflags\t\t: fpu sse4_2 avx avx2 fma f16c bmi2\n"

    @Test
    fun oldIntelCpuNeedsTheCompatBuild() {
        assertEquals(listOf("avx2", "fma", "f16c"), DesktopNativeLoader.missingBaseline(sandyBridge, "linux", "amd64"))
        assertEquals(listOf("llama_jni_compat"), DesktopNativeLoader.candidates("linux", "amd64", sandyBridge, vulkan = false))
    }

    @Test
    fun modernCpuUsesTheDefaultBuild() {
        assertEquals(listOf("llama_jni"), DesktopNativeLoader.candidates("linux", "amd64", haswell, vulkan = false))
    }

    @Test
    fun vulkanBuildIsTriedFirstAndMatchesTheCpuLevel() {
        assertEquals(listOf("llama_jni_vulkan", "llama_jni"), DesktopNativeLoader.candidates("linux", "x86_64", haswell, vulkan = true))
        assertEquals(
            listOf("llama_jni_vulkan_compat", "llama_jni_compat"),
            DesktopNativeLoader.candidates("linux", "x86_64", sandyBridge, vulkan = true),
        )
    }

    @Test
    fun unknownCpuInfoAndOtherPlatformsKeepTheDefault() {
        assertEquals(listOf("llama_jni"), DesktopNativeLoader.candidates("linux", "amd64", null, vulkan = false))
        assertEquals(listOf("llama_jni"), DesktopNativeLoader.candidates("linux", "aarch64", sandyBridge, vulkan = true))
        assertEquals(listOf("llama_jni"), DesktopNativeLoader.candidates("macos", "aarch64", sandyBridge, vulkan = true))
        assertEquals(listOf("llama_jni"), DesktopNativeLoader.candidates("windows", "amd64", sandyBridge, vulkan = true))
    }
}
