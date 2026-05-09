plugins {
    kotlin("jvm") version "2.3.20"
    application
    id("io.ktor.plugin") version "3.4.3"
}

application {
    mainClass.set("io.ktor.samples.clientmultipart.MultipartAppKt")
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
    implementation("ch.qos.logback:logback-classic:1.5.32")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
}

kotlin {
    jvmToolchain(17)
}