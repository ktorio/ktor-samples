package io.ktor.samples.httpbin

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, 8080) {
        module()
    }.start(wait = true)
}

// TODO: Add Status pages
// TODO: Add swagger docs to the main page
// TODO: Move routes code based on the group to a separate file
