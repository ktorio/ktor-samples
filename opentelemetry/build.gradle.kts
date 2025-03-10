description = "OpenTelemetry-Ktor example"

plugins {
    id("com.avast.gradle.docker-compose") version "0.17.12"
}

subprojects {
    group = "opentelemetry.ktor.example"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}

dockerCompose {
    useComposeFiles.add("docker/docker-compose.yml")
}

tasks.register("runWithDocker") {
    dependsOn("composeUp", ":server:run")
}

project(":server").setEnvironmentVariablesForOpenTelemetry()
project(":client").setEnvironmentVariablesForOpenTelemetry()

fun Project.setEnvironmentVariablesForOpenTelemetry() {
    tasks.withType<JavaExec> {
        environment("OTEL_METRICS_EXPORTER", "none")
        environment("OTEL_EXPORTER_OTLP_ENDPOINT", "http://localhost:4317/")
    }
}