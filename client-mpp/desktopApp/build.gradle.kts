plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    // on Linux X64
    // linuxX64("desktop")
    // on Windows x64
    // mingwX64("desktop")
    // on MacOs X64
    // macosX64("desktop")
    // on MacOS Arm64
    macosArm64("desktop") {
        binaries {
            executable()
        }
    }
    sourceSets {
        nativeMain {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }
}
