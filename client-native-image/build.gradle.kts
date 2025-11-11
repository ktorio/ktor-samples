plugins {
    kotlin("jvm") version "2.2.21"
    application
    id("org.graalvm.buildtools.native") version "0.11.3"
}

group = "io.ktor.samples"
version = "1.0-SNAPSHOT"

val mainClassName = "io.ktor.samples.client.AppKt"
application {
    mainClass = mainClassName
}

graalvmNative {
    binaries {
        named("main") {
            imageName = "ktor-client-native-image"
            mainClass = mainClassName
            buildArgs.add("-O4")
        }
        named("test") {
            buildArgs.add("-O0")
        }
        all {
            buildArgs.add("--verbose")
        }
    }

    toolchainDetection = true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-apache:3.3.2")
    implementation("io.ktor:ktor-client-cio:3.3.2")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}