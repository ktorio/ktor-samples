plugins {
    application
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    id("org.graalvm.buildtools.native") version "0.9.19"
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
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-cio-jvm")

    testImplementation("io.ktor:ktor-server-test-host-jvm")
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