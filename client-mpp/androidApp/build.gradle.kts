import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    compileSdk = 34
    defaultConfig {
        applicationId = "io.ktor.samples.mpp.client"
        minSdk = 30
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
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
    namespace = "io.ktor.samples.mpp.client"
}
