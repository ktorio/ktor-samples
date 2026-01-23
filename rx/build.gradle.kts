val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
}

application {
    mainClass.set("io.ktor.samples.rx.RxApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.10.2")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}

kotlin {
    jvmToolchain(17)
}