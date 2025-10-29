package com.example

import io.ktor.server.application.*
import com.example.plugins.*
import com.example.service.ArticleService

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSerialization()
    val articleService = ArticleService()
    configureRouting(articleService = articleService)
    environment.monitor.subscribe(ApplicationStarted) { application ->
        application.environment.log.info("Server is started")
    }
    environment.monitor.subscribe(ApplicationStopped) { application ->
        application.environment.log.info("Server is stopped")
        articleService.release()
        application.environment.monitor.unsubscribe(ApplicationStarted) {}
        application.environment.monitor.unsubscribe(ApplicationStopped) {}
    }
}
