package io.ktor.samples.testable

import io.ktor.application.Application
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.samples.sessions.SampleSession
import io.ktor.samples.sessions.main
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRequestWithoutSession() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/view")) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun testRequestWithSession() = withTestApplication(Application::main) {
        with(handleRequest(HttpMethod.Get, "/view") {
            // assume i actually care about my application security
            // and have to set httpOnly cookies from backend
            this.call.sessions.set(SampleSession(0))
        }) {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }
}
