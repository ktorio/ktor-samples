val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
}

application {
    mainClass.set("io.ktor.samples.openapi.ApplicationKt")
}

repositories {
    mavenCentral()
}

ktor {
    @OptIn(io.ktor.plugin.OpenApiPreview::class)
    openApi {
        title = "OpenAPI example"
        version = "2.1"
        summary = "This is a sample API"
    }
}

// Builds OpenAPI specification automatically
tasks.processResources {
    dependsOn("buildOpenApi")
}

dependencies {
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.openapi)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.apache)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.config.yaml)
    implementation("ch.qos.logback:logback-classic:1.5.21")
}

kotlin {
    jvmToolchain(17)
}