pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "client-mpp"

include(":androidApp")
include(":desktopApp")
include(":jsApp")
include(":shared")
