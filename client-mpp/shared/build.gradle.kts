plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
        macosX64(),
        macosArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    js(IR) {
        browser()
    }

    sourceSets.forEach {
        it.dependencies {
            implementation(project.dependencies.enforcedPlatform("io.ktor:ktor-bom:3.3.1"))
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("io.ktor:ktor-client-core")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        androidMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
                implementation("io.ktor:ktor-client-android")
            }
        }
        androidUnitTest {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        iosMain {
            dependencies {
                implementation("io.ktor:ktor-client-ios")
            }
        }
        iosTest {

        }
        jsMain {
            dependencies {
                implementation("io.ktor:ktor-client-js")
            }
        }
        macosArm64Main {
            dependencies {
                implementation("io.ktor:ktor-client-core")
                implementation("io.ktor:ktor-client-curl")
            }
        }
    }
}

android {
    compileSdk = 34
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 19
        lint.targetSdk = 33
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    packaging {
        resources.excludes.add("META-INF/*.kotlin_module")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    namespace = "o.ktor.samples.mpp.client.shared"
}
