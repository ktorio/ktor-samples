plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    js("frontend", IR) {
        browser {
            testTask {
                enabled = false
            }

            @OptIn(org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl::class)
            distribution {
                outputDirectory.set(file("$projectDir/src/backendMain/resources/js"))
            }
            binaries.executable()
        }
    }
    jvm("backend")

    sourceSets.all {
        dependencies {
            implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:3.4.0"))
        }
    }

    sourceSets {
        val backendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.3.0")
                implementation("io.ktor:ktor-server-netty")
                implementation("io.ktor:ktor-server-websockets")
                implementation("io.ktor:ktor-server-call-logging")
                implementation("io.ktor:ktor-server-default-headers")
                implementation("io.ktor:ktor-server-sessions")
                implementation("ch.qos.logback:logback-classic:1.5.21")
            }
        }

        val backendTest by getting {
            dependencies {
                implementation("io.ktor:ktor-server-test-host")
                implementation("io.ktor:ktor-client-websockets")
                implementation("org.jetbrains.kotlin:kotlin-test")
            }
        }

        val frontendMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
                implementation("io.ktor:ktor-client-websockets")
                implementation("io.ktor:ktor-client-js")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.10.2")
            }
        }
    }

    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

tasks.named("backendProcessResources") {
    dependsOn("frontendBrowserProductionWebpack")
    dependsOn("frontendBrowserDistribution")
}

tasks.register<JavaExec>("run") {
    dependsOn(tasks.named("frontendBrowserDistribution"))
    dependsOn(tasks.named("backendMainClasses"))
    mainClass.set("io.ktor.samples.chat.backend.ChatApplicationKt")
    classpath(configurations.named("backendRuntimeClasspath"), tasks.named("backendJar"))
    args = listOf()
}
