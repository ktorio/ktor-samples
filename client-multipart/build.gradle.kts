plugins {
    kotlin("jvm") version "2.4.10"
    application
    id("io.ktor.plugin") version "3.5.2"
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
    implementation("ch.qos.logback:logback-classic:1.6.3")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
}

kotlin {
    jvmToolchain(17)
}