plugins {
    application
    kotlin("jvm") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.1"
}

group = "io.ktor"
version = "0.0.1"
application {
    mainClass.set("io.ktor.ApplicationKt")
    mainClassName = "io.ktor.ApplicationKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.2.9")
    implementation("io.ktor:ktor-server-core-jvm:2.0.1")
    implementation("io.ktor:ktor-server-cio-jvm:2.0.1")
}


