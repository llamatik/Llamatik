plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("maven-publish")
    id("signing")
}

group = "com.llamatik"
version = (System.getenv("RELEASE_VERSION") ?: "0.0.0-SNAPSHOT")

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

// The Llamatik inference HTTP API (/v1/...) as an engine-agnostic Ktor library: routes, services,
// SSE streaming, per-device approval. No Tomcat/CIO, no database, no JWT, no mDNS — the host
// picks the engine and calls Application.installLlamatikServer(...):
//   * :host            runs it inside the app (Ktor CIO) so a device can serve its models
//   * Llamatik-Server  runs it standalone (Tomcat) — consumes the published com.llamatik:server
dependencies {
    // `api`: hosts need the bridges the routes call. Consumers get the same version as this build.
    api(project(":core"))
    api(libs.ktor.server.core)
    api(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.kotlinx.json)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "server"
            from(components["java"])
            pom {
                name.set("Llamatik Server")
                description.set("Llamatik inference HTTP API (Ktor routes) for embedding in apps or running standalone.")
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
}

signing {
    val signingKey = (findProperty("signingInMemoryKey") as String?) ?: System.getenv("SIGNING_KEY")
    val signingPassword = (findProperty("signingInMemoryKeyPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")
    if (!signingKey.isNullOrBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}
