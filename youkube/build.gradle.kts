val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    kotlin("plugin.serialization") version "2.1.10"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-compression")
    implementation("io.ktor:ktor-server-conditional-headers")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-partial-content")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.ehcache:ehcache:3.9.7")
    implementation("com.google.code.gson:gson:2.9.1")
    implementation("io.ktor:ktor-server-netty-jvm")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}