buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
        classpath("com.android.tools.build:gradle:8.6.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}