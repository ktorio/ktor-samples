package io.ktor.samples.rx

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.reactivex.*
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.*
import java.util.concurrent.*

/**
 * Documentation: https://github.com/Kotlin/kotlinx.coroutines/tree/master/reactive/kotlinx-coroutines-rx2
 */
fun main() {
    embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                val result = Flowable.range(1, 10)
                    .map { it * it }
                    .delay(300L, TimeUnit.MILLISECONDS)
                    .awaitLast()

                call.respondText("LAST ITEM: $result")
            }
            get("/iter") {
                call.respondTextWriter(ContentType.Text.Plain) {
                    val writer = this
                    Flowable.range(1, 10)
                        .map { it * it }
                        .delay(300L, TimeUnit.MILLISECONDS)
                        .collect {
                            writer.write("$it,")
                            writer.flush()
                            delay(100L)
                        }
                }
            }
        }
    }.start(wait = true)
}
