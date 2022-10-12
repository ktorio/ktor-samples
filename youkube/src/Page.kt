package io.ktor.samples.youkube

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.html.HtmlContent
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.date.*
import kotlinx.html.*

/**
 * Generates HTML for the structure of the page and allows to provide a [block] that will be placed
 * in the content place of the page.
 */
suspend fun ApplicationCall.respondDefaultHtml(
    versions: List<Version>,
    visibility: CacheControl.Visibility,
    title: String = "YouKube",
    block: DIV.() -> Unit
) {
    val content = HtmlContent(HttpStatusCode.OK) {
        val session = sessions.get<YouKubeSession>()
        head {
            title { +title }
            styleLink("http://yui.yahooapis.com/pure/0.6.0/pure-min.css")
            styleLink("http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css")
            styleLink(request.origin.run {
                "$scheme://$host:$port${application.href(MainCss())}"
            })
        }
        body {
            div("pure-g") {
                div("sidebar pure-u-1 pure-u-md-1-4") {
                    div("header") {
                        div("brand-title") { +title }
                        div("brand-tagline") {
                            if (session != null) {
                                +session.userId
                            }
                        }

                        nav("nav") {
                            ul("nav-list") {
                                li("nav-item") {
                                    if (session == null) {
                                        a(classes = "pure-button", href = application.href(Login())) { +"Login" }
                                    } else {
                                        a(classes = "pure-button", href = application.href(Upload())) { +"Upload" }
                                    }
                                }
                                li("nav-item") {
                                    a(classes = "pure-button", href = application.href(Index())) { +"Watch" }
                                }
                            }
                        }
                    }
                }

                div("content pure-u-1 pure-u-md-3-4") {
                    block()
                }
            }
        }
    }
    content.versions = versions
    content.caching = CachingOptions(
        cacheControl = CacheControl.MaxAge(
            3600 * 24 * 7,
            mustRevalidate = true,
            visibility = visibility,
            proxyMaxAgeSeconds = null,
            proxyRevalidate = false
        ),
        expires = (null as? GMTDate?)
    )
    respond(content)
}


