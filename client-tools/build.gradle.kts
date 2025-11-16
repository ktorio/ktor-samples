plugins {
    kotlin("jvm") version "2.2.21"
    application
    id("io.ktor.plugin") version "3.3.2"
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.21")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("ch.qos.logback:logback-classic:1.5.21")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-cio-jvm")
}

kotlin {
    jvmToolchain(17)
}