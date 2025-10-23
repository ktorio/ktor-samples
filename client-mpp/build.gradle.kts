buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
        classpath("com.android.tools.build:gradle:8.13.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}