plugins {
    application
    kotlin("jvm") version "1.7.20"
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
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.ktor:ktor-server-core-jvm:2.2.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.20")
    testImplementation("io.ktor:ktor-server-tests-jvm:2.2.0")
}
