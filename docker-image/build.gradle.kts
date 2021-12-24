plugins {
    application
    kotlin("jvm") version "1.6.10"
    id("com.google.cloud.tools.jib") version "3.1.4"
}

group = "io.ktor.samples.deployment"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.0.0-beta-1")
    implementation("io.ktor:ktor-server-netty:2.0.0-beta-1")
    implementation("ch.qos.logback:logback-classic:1.2.9")
    testImplementation("io.ktor:ktor-server-tests:2.0.0-beta-1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.6.10")
}
