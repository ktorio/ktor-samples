plugins {
    application
    kotlin("jvm") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "io.ktor"
version = "0.0.1"
application {
    mainClass.set("io.ktor.ApplicationKt")
    mainClassName = "io.ktor.ApplicationKt"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.0.0-eap-256")
    implementation("io.ktor:ktor-server-cio:2.0.0-eap-256")
    implementation("ch.qos.logback:logback-classic:1.2.6")
}


