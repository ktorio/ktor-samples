buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
        classpath("com.android.tools.build:gradle:8.13.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}