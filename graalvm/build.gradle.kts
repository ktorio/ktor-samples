plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.2.4"
    id("org.graalvm.buildtools.native") version "0.9.11"
}

group = "io.ktor"
version = "0.0.1"
application {
    mainClass.set("io.ktorgraal.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")
}

graalvmNative {
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            imageName.set("graal-server")
        }
    }
}