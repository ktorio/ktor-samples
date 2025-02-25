val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
}

application {
    mainClass.set("io.ktor.samples.filelisting.FileListingApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-call-logging")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}