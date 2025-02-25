val opentelemetry_version: String by project
val opentelemetry_semconv_version: String by project
val opentelemetry_exporter_otlp_version: String by project
val opentelemetry_sdk_extension_autoconfigure_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
}

dependencies {
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:$opentelemetry_sdk_extension_autoconfigure_version");
    implementation("io.opentelemetry:opentelemetry-exporter-otlp:$opentelemetry_exporter_otlp_version");
    implementation("io.opentelemetry.semconv:opentelemetry-semconv:$opentelemetry_semconv_version")

    implementation("io.ktor:ktor-server-core-jvm")
}