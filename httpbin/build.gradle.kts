val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auto-head-response")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-conditional-headers")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-serialization-gson")
    implementation("io.ktor:ktor-server-sessions-jvm")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-partial-content")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}
