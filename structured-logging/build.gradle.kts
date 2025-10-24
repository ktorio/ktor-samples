val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.21"
    id("io.ktor.plugin") version "3.3.1"
}

application {
    mainClass.set("io.ktor.samples.structuredlogging.ApplicationKt")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers/")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.860")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:8.1")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}