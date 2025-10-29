plugins {
    application
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.1"
    id("org.graalvm.buildtools.native") version "0.11.2"
    kotlin("plugin.serialization") version "2.2.21"
}

group = "io.ktor"
version = "0.0.1"

application {
    mainClass.set("io.ktorgraal.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.20")
    implementation("io.ktor:ktor-server-cio")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

graalvmNative {
    binaries {

        named("main") {
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("--initialize-at-build-time=org.slf4j.helpers.Reporter")
            buildArgs.add("--initialize-at-build-time=kotlinx.io.bytestring.ByteString")
            buildArgs.add("--initialize-at-build-time=kotlinx.io.SegmentPool")

            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.Json")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.JsonImpl")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.ClassDiscriminatorMode")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.modules.SerializersModuleKt")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            imageName.set("graalvm-server")
        }

        named("test"){
            fallback.set(false)
            verbose.set(true)

            buildArgs.add("--initialize-at-build-time=ch.qos.logback")
            buildArgs.add("--initialize-at-build-time=io.ktor,kotlin")
            buildArgs.add("--initialize-at-build-time=org.slf4j.LoggerFactory")

            buildArgs.add("--initialize-at-build-time=org.slf4j.helpers.Reporter")
            buildArgs.add("--initialize-at-build-time=kotlinx.io.bytestring.ByteString")
            buildArgs.add("--initialize-at-build-time=kotlinx.io.SegmentPool")

            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.Json")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.JsonImpl")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.json.ClassDiscriminatorMode")
            buildArgs.add("--initialize-at-build-time=kotlinx.serialization.modules.SerializersModuleKt")

            buildArgs.add("-H:+InstallExitHandlers")
            buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
            buildArgs.add("-H:+ReportExceptionStackTraces")

            val path = "${projectDir}/src/test/resources/META-INF/native-image/"
            buildArgs.add("-H:ReflectionConfigurationFiles=${path}reflect-config.json")
            buildArgs.add("-H:ResourceConfigurationFiles=${path}resource-config.json")

            imageName.set("graalvm-test-server")
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
