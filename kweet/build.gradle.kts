val kotlin_version: String by project
val logback_version: String by project
val exposed_version: String by project

plugins {
    kotlin("jvm") version "2.1.10"
    id("io.ktor.plugin") version "3.1.1"
    kotlin("plugin.serialization") version "2.1.10"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-freemarker")
    implementation("io.ktor:ktor-server-resources")
    implementation("io.ktor:ktor-server-sessions")
    implementation("io.ktor:ktor-server-conditional-headers")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-partial-content")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("com.h2database:h2:2.1.214")
    implementation("com.mchange:c3p0:0.9.5.5")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposed_version")
    implementation("joda-time:joda-time:2.12.2")
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("org.ehcache:ehcache:3.9.7")
    implementation("io.ktor:ktor-server-netty-jvm")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
}
