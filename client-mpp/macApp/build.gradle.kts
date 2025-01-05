plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    macosArm64("native") { // on macOS
        // linuxX64("native") // on Linux
        // mingwX64("native") // on Windows
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
