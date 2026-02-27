val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
}

application {
    mainClass.set("io.ktor.samples.structuredlogging.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:2025.12.12")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:9.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}

kotlin {
    jvmToolchain(17)
}
