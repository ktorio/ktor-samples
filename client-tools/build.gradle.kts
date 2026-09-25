plugins {
    kotlin("jvm") version "2.4.20"
    application
    id("io.ktor.plugin") version "3.6.0"
}

application {
    mainClass.set("ToolsAppKt")
}

sourceSets {
    main {
        kotlin.srcDirs("src")
        resources.srcDirs("resources")
    }
    test {
        kotlin.srcDirs("test")
        resources.srcDirs("testresources")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-html-builder")
    implementation("ch.qos.logback:logback-classic:1.6.4")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
}

kotlin {
    jvmToolchain(17)
}