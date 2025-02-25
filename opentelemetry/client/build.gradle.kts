val ktor_version: String by project
val logback_version: String by project
val kotlin_version: String by project
val opentelemetry_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    id("application")
}

application {
    mainClass.set("opentelemetry.ktor.example.ClientKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":shared"))

    implementation("io.ktor:ktor-client-core-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.opentelemetry.instrumentation:opentelemetry-ktor-3.0:$opentelemetry_version-alpha")
}