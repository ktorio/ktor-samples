val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.4.10"
    id("io.ktor.plugin") version "3.5.2"
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("io.ktor.samples.location.LocationHeaderApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
}
