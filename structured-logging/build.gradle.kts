val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.8.0"
    id("io.ktor.plugin") version "2.2.3"
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
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-css:1.0.0-pre.457")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:7.1.1")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
}