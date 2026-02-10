rootProject.name = "WasmProject"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://redirector.kotlinlang.org/maven/compose-dev")
        maven("https://redirector.kotlinlang.org/maven/ktor-eap")
    }
}

include(":composeApp")