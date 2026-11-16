import com.android.build.api.dsl.LibraryExtension
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    id("com.android.library")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
}

group = "com.llamatik"
version = (System.getenv("RELEASE_VERSION") ?: "0.0.0-SNAPSHOT")

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }

    jvm()

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            implementation(libs.ktor.client)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.server.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.java)
                implementation(libs.jmdns)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

extensions.configure<LibraryExtension> {
    namespace = "com.llamatik.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// Maven Central requires a -javadoc.jar on every publication. KMP publications ship none by
// default, so attach an empty one (Central only checks that it exists).
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(javadocJar)
        pom {
            name.set("Llamatik Network")
            description.set("Client for remote Llamatik servers: distributed inference, mDNS discovery and LlamatikServerClient.")
            url.set("https://github.com/ferranpons/llamatik")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("ferranpons")
                    name.set("Ferran Pons")
                    url.set("https://github.com/ferranpons/llamatik")
                }
            }
            scm {
                url.set("https://github.com/ferranpons/llamatik")
                connection.set("scm:git:git://github.com/ferranpons/llamatik.git")
                developerConnection.set("scm:git:ssh://github.com/ferranpons/llamatik.git")
            }
        }
    }
}

signing {
    val signingKey = (findProperty("signingInMemoryKey") as String?) ?: System.getenv("SIGNING_KEY")
    val signingPassword = (findProperty("signingInMemoryKeyPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")
    if (!signingKey.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

// All KMP publications share the same javadoc jar, so each Sign task writes an output (the .asc)
// that other publications' publish tasks consume. Gradle 9 rejects that as an implicit dependency;
// make every publish task wait for all signing tasks.
tasks.withType<AbstractPublishToMaven>().configureEach {
    dependsOn(tasks.withType<Sign>())
}
