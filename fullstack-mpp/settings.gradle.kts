rootProject.name = "ktor-samples"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

fun module(group: String, name: String) {
    include(name)
    project(":$name").projectDir = file("$group/$name")
}

// ---------------------------

module("samples", "fullstack-mpp")
