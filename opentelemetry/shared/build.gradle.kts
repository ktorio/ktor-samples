val opentelemetry_version: String by project
val opentelemetry_semconv_version: String by project

plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "3.0.1"
}

dependencies {
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:$opentelemetry_version");
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:$opentelemetry_version");
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:$opentelemetry_semconv_version")

    implementation("io.ktor:ktor-server-core-jvm")
}