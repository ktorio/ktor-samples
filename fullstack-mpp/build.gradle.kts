import org.jetbrains.kotlin.gradle.targets.js.webpack.*

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0"
}

kotlin {
    targets {
        js("frontend", IR) {
            browser {
                testTask {
                    // TODO: disable browser tests since we can"t run it on teamcity agents yet
                    enabled = false
                }
                webpackTask {
                    mainOutputFileName = "output.js"
                }
                binaries.executable()
            }
        }
        jvm("backend") {
        }
    }

    sourceSets.forEach {
        it.dependencies {
            implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:3.1.1"))
        }
    }

    sourceSets {
        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
                implementation("org.jetbrains.kotlin:kotlin-test-common")
            }
        }
    }

    sourceSets {
        val backendMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-html-builder")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("ch.qos.logback:logback-classic:1.5.17")
            }
        }
        val backendTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test")
                implementation("org.jetbrains.kotlin:kotlin-test-junit")
            }
        }
        val frontendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("org.jetbrains.kotlin:kotlin-test-js")
                implementation("io.ktor:ktor-client-core")
                implementation("io.ktor:ktor-client-js")
            }
        }
    }
}

repositories {
    mavenCentral()
}

val backendJar = tasks.named<Jar>("backendJar") {
    val frontendBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("frontendBrowserProductionWebpack")
    dependsOn(frontendBrowserProductionWebpack)
    from(frontendBrowserProductionWebpack.outputDirectory, frontendBrowserProductionWebpack.mainOutputFileName)
}

tasks.register<JavaExec>("run") {
    dependsOn(backendJar)
    mainClass.set("io.ktor.samples.fullstack.backend.BackendCodeKt")
    classpath = files(configurations.getByName("backendRuntimeClasspath"), backendJar)
    args = listOf<String>()
}
