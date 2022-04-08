plugins {
    application
    kotlin("jvm") version "1.6.20"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "io.ktor.samples.deployment"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.0.0")
    implementation("io.ktor:ktor-server-netty:2.0.0")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    testImplementation("io.ktor:ktor-server-tests:2.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.20")
}
