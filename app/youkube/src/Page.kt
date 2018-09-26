package io.ktor.samples.youkube

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.html.HtmlContent
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.locations.*
import io.ktor.response.*
import io.ktor.sessions.*
import io.ktor.util.date.*
import kotlinx.html.*

/**
 * Function that generates HTML for the structure of the page and allows to provide a [block] that will be placed
 * in the content place of the page.
 */
suspend fun ApplicationCall.respondDefaultHtml(versions: List<Version>, visibility: CacheControl.Visibility, title: String = "You Kube", block: DIV.() -> Unit) {
    val content = HtmlContent(HttpStatusCode.OK) {
        val session = sessions.get<YouKubeSession>()
        head {
            title { +title }
            styleLink("http://yui.yahooapis.com/pure/0.6.0/pure-min.css")
            styleLink("http://yui.yahooapis.com/pure/0.6.0/grids-responsive-min.css")
            styleLink(url(MainCss()) {
                protocol = URLProtocol.createOrDefault(request.origin.scheme)
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
                                        a(classes = "pure-button", href = locations.href(Login())) { +"Login" }
                                    } else {
                                        a(classes = "pure-button", href = locations.href(Upload())) { +"Upload" }
                                    }
                                }
                                li("nav-item") {
                                    a(classes = "pure-button", href = locations.href(Index())) { +"Watch" }
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
            cacheControl = CacheControl.MaxAge(3600 * 24 * 7, mustRevalidate = true, visibility = visibility, proxyMaxAgeSeconds = null, proxyRevalidate = false),
            expires = (null as? GMTDate?)
    )
    respond(content)
}


