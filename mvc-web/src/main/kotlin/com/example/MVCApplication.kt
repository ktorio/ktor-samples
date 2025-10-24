package com.example

import com.example.model.DatabaseSingleton
import com.example.plugins.configureRouting
import com.example.plugins.configureTemplating
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseSingleton.init(
        environment.config.property("ktor.application.datasource.url").getString(),
        environment.config.property("ktor.application.datasource.driverClassName").getString()
    )

    configureTemplating()
    configureRouting()
}
