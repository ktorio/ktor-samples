val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
}

application {
    mainClass.set("io.ktor.samples.reverseproxy.ReverseProxyApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-html-builder")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
}