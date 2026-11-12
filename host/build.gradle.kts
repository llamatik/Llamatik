import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.library")
}

// :host lets the app act as a Llamatik server for other devices on the network.
// The API itself is NOT implemented here: it comes from the Llamatik-Server library
// (:server-lib, published as com.llamatik:server), the same code the standalone server runs. This module only adds
// the engine (Ktor CIO), lifecycle, device approval persistence and platform glue.
kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    jvm()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        // The same server implementation is compiled for Android and desktop.
        val jvmShared = "src/jvmShared/kotlin"
        androidMain { kotlin.srcDir(jvmShared) }
        jvmMain { kotlin.srcDir(jvmShared) }

        commonMain.dependencies {
            api(project(":core"))
            implementation(project(":network"))
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            implementation(project(":server-lib"))
            implementation(libs.ktor.server.cio)
            implementation(libs.androidx.core.ktx)
        }

        jvmMain.dependencies {
            implementation(project(":server-lib"))
            implementation(libs.ktor.server.cio)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

extensions.configure<LibraryExtension> {
    namespace = "com.llamatik.host"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
