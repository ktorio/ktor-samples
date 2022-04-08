import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-cio:2.0.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.0")
    implementation("io.ktor:ktor-client-logging:2.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-xml:2.0.0")
    implementation("io.ktor:ktor-server-default-headers:2.0.0")
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-auth:2.0.0")
    implementation("ch.qos.logback:logback-classic:1.2.6")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}