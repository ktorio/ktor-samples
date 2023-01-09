package io.ktor.samples.httpbin

import com.google.gson.*
import com.google.gson.reflect.*
import io.ktor.content.TextContent
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.conditionalheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.partialcontent.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.date.*
import io.ktor.util.logging.*
import kotlinx.coroutines.*
import java.io.*
import java.time.*
import java.util.*

/**
 * A Gson Builder with pretty printing enabled.
 */
val gson: Gson = GsonBuilder().setPrettyPrinting().create()

/**
 * The entrypoint / main module. Referenced from resources/application.conf#ktor.application.modules
 *
 * More information about the application.conf file here: https://ktor.io/docs/configurations.html#configuration-file
 */
fun Application.main() {
    /**
     * Install all the plugins we are going to use.
     */
    // This plugin sets a Date and Server headers automatically.
    install(DefaultHeaders)
    // Logs all the requests performed
    install(CallLogging)
    // Automatic '304 Not Modified' Responses
    install(ConditionalHeaders)
    // Supports for Range, Accept-Range and Content-Range headers
    install(PartialContent)
    // For each GET header, adds an automatic HEAD handler (checks the headers of the requests
    // without actually getting the payload to be more efficient about resources)
    install(AutoHeadResponse)
    // Based on the Accept header, allows replying with arbitrary objects converting them into JSON
    // when the client accepts it.
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter(gson))
    }
    // Enables Cross-Origin Resource Sharing (CORS)
    install(CORS) {
        anyHost()
        listOf(HttpMethod("PATCH"), HttpMethod.Put, HttpMethod.Delete).forEach {
            allowMethod(it)
        }
    }
    // Here we handle unhandled exceptions from routes
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            this@main.environment.log.error(cause)
            val error = HttpBinError(
                code = HttpStatusCode.InternalServerError,
                request = call.request.local.uri,
                message = cause.toString(),
                cause = cause
            )
            call.respond(error)
        }
    }

    // Fake Authorization with user:password "test:test"
    val hashedUserTable = UserHashedTableAuth(
        getDigestFunction("SHA-256") { "ktor${it.length}" },
        table = mapOf(
            "test" to Base64.getDecoder().decode("GSjkHCHGAxTTbnkEDBbVYd+PUFRlcWiumc4+MWE9Rvw=") // sha256 for "test"
        )
    )

    // We will register all the available routes here
    routing {
        // Route to test plain 'get' requests.
        // ApplicationCall.sendHttpBinResponse is an extension method defined in this project that sends
        // information about the request as an object, that will be converted into JSON
        // by the ContentNegotiation plugin.
        get("/get") {
            call.sendHttpBinResponse()
        }

        // This is a sample of registering routes "dynamically".
        // We define a map with a pair 'path' to 'method' and then we register it.
        val postPutDelete = mapOf(
            "/post" to HttpMethod.Post,
            "/put" to HttpMethod.Put,
            "/delete" to HttpMethod.Delete,
            "/patch" to HttpMethod("PATCH")
        )
        for ((route, method) in postPutDelete) {
            route(route) {
                // This method will register different handlers for this route ('path' to 'method')
                // depending on the Content-Type provided by the client for the content it is going to send.
                // Since GET or HEAD requests do not have content, it is not applicable for those methods.
                // This handles [ContentType.MultiPart.FormData], [ContentType.Application.FormUrlEncoded],
                // [ContentType.Application.Json] and others.
                handleRequestWithBodyFor(method)
            }
        }

        // Defines an '/image' route that will serve different content, based on the 'Accept' header sent by the client.
        route("/image") {
            val imageConfigs = listOf(
                ImageConfig("jpeg", ContentType.Image.JPEG, "jackal.jpg"),
                ImageConfig("png", ContentType.Image.PNG, "pig_icon.png"),
                ImageConfig("svg", ContentType.Image.SVG, "svg_logo.svg"),
                ImageConfig("webp", ContentType("image", "webp"), "wolf_1.webp"),
                ImageConfig("any", ContentType.Image.Any, "jackal.jpg")
            )
            for ((path, contentType, filename) in imageConfigs) {
                // Serves this specific file in the specific format in the route when the 'Accept' header makes it the best match.
                // So, for example, a Chrome browser would receive a WEBP image,
                // while another browser like Internet Explorer would receive a JPEG.
                accept(contentType) {
                    resource("", "static/$filename")
                }
                // As a fallback, we also serve the file independently on the Accept header, in the `/image/format` route.
                resource(path, "static/$filename")
            }
        }

        // This route sends a response that will include the Headers sent from the client.
        get("/headers") {
            call.sendHttpBinResponse {
                clear()
                headers = call.request.headers.toMap()
            }
        }

        // This route includes the IP of the client. In the case this server is behind a reverse-proxy,
        // you can also register the ForwardedHeaderSupport plugin, and the `call.request.origin.remoteHost`
        // would return the user's IP, while `call.request.local.remoteHost` would return the IP of the reverse proxy.
        get("/ip") {
            call.sendHttpBinResponse {
                clear()
                origin = call.request.origin.remoteHost
            }
        }

        // Forces a gzipped response
        route("/gzip") {
            install(Compression) {
                gzip()
            }
            get {
                call.sendHttpBinResponse {
                    gzipped = true
                }
            }
        }

        // Forces a deflated response
        route("/deflate") {
            install(Compression) {
                deflate()
            }
            get {
                call.sendHttpBinResponse {
                    deflated = true
                }
            }
        }

        // This can be done using the [ConditionalHeaders] plugin and setting the
        // ETag and Last-Modified headers to the response content.
        get("/cache") {
            val etag = "db7a0a2684bb439e858ee25ae5b9a5c6"
            val date: ZonedDateTime = ZonedDateTime.of(2016, 2, 15, 0, 0, 0, 0, ZoneId.of("Z")) // Kotlin 1.0
            call.response.header(HttpHeaders.LastModified, date)
            call.response.header(HttpHeaders.ETag, etag)
            call.response.lastModified(date)
            call.sendHttpBinResponse()
        }

        // This route sets the Cache Control header to have a maxAge to [n] seconds.
        get("/cache/{n}") {
            val n = call.parameters["n"]!!.toInt()
            val cache = CacheControl.MaxAge(maxAgeSeconds = n, visibility = CacheControl.Visibility.Public)
            call.response.cacheControl(cache)
            call.sendHttpBinResponse()
        }

        // Returns the User-Agent header sent by the client.
        get("/user-agent") {
            call.sendHttpBinResponse {
                clear()
                `user-agent` = call.request.header("User-Agent")
            }
        }

        // Returns an HTTP status code based on the {status} URL parameter.
        get("/status/{status}") {
            val status = call.parameters["status"]?.toInt() ?: 0
            call.respond(HttpStatusCode.fromValue(status))
        }

        // Returns an HTML page with an ul list of [n] links, and the [m]th link will be selected (unclickable).
        get("/links/{n}/{m?}") {
            try {
                val nbLinks = call.parameters["n"]!!.toInt()
                val selectedLink = call.parameters["m"]?.toInt() ?: 0
                call.respondHtml {
                    generateLinks(nbLinks, selectedLink)
                }
            } catch (e: Throwable) {
                call.respondHtml(status = HttpStatusCode.BadRequest) {
                    invalidRequest("$e")
                }
            }
        }

        // Responds with a text saying that you shouldn't be here.
        get("/deny") {
            call.respondText(ANGRY_ASCII)
        }

        // Throws an exception that will be handled by the [StatusPages] plugin installed and configured above.
        get("/throw") {
            throw RuntimeException("Endpoint /throw thrown a throwable")
        }

        // Responds with the headers specified by the queryParameters. So, for example
        //
        // - /response-headers?Location=/deny -- Would generate a header 'Location' to redirect to '/deny'
        //
        // Also it responds with a JSON with the specified query parameters
        get("/response-headers") {
            val params = call.request.queryParameters
            val requestedHeaders = params.flattenEntries().toMap()
            for ((key, value) in requestedHeaders) {
                call.response.header(key, value)
            }
            val content = TextContent(gson.toJson(params), ContentType.Application.Json)
            call.respond(content)
        }

        // Generates a redirection chain. Just like a recursive function.
        // - /redirect/10  -- would redirect to /redirect/9.
        // - /redirect/0   -- wouldn't redirect
        // This is useful for testing maximum redirections from HTTP clients.
        get("/redirect/{n}") {
            val n = call.parameters["n"]!!.toInt()
            if (n == 0) {
                call.sendHttpBinResponse()
            } else {
                call.respondRedirect("/redirect/${n - 1}")
            }
        }

        // Generates a temporal redirection [HttpStatusCode.Found] to the URL specified in the path.
        get("/redirect-to/{url}") {
            val url = call.parameters["url"]!!
            call.respondRedirect(url)
        }

        // @TODO: Generates a redirection relative to this path
        get("/relative-redirect/{n}") {
            call.parameters["n"]!!.toInt()
            TODO("302 Relative redirects n times.")
        }

        // @TODO: Generates a redirection absolute to this path
        get("/absolute-redirect/{n}") {
            call.parameters["n"]!!.toInt()
            TODO("302 Absolute redirects n times.")
        }

        // Returns the list of raw cookies sent by the client
        get("/cookies") {
            val rawCookies = call.request.cookies.rawCookies
            call.sendHttpBinResponse {
                clear()
                cookies = rawCookies
            }
        }

        // Generates a response that will instruct to set cookies based on the query parameters sent by the client.
        get("/cookies/set") {
            val params = call.request.queryParameters.flattenEntries()
            for ((key, value) in params) {
                call.response.cookies.append(name = key, value = value, path = "/")
            }
            val rawCookies = call.request.cookies.rawCookies
            call.sendHttpBinResponse {
                clear()
                cookies = rawCookies + params.toMap()
            }
        }

        // Generates a response that will set expired cookies based on the query parameters sent by the client.
        get("/cookies/delete") {
            val params = call.request.queryParameters.names()
            val rawCookies = call.request.cookies.rawCookies
            for (name in params) {
                call.response.cookies.append(name, "", path = "/", expires = GMTDate.START)
            }
            call.sendHttpBinResponse {
                clear()
                cookies = rawCookies.filterKeys { key -> key !in params }
            }
        }

        // Register a route that uses the basic Authentication plugin to request a user/password to the user when
        // no user/password is provided or is invalid, and handles the request if the authentication is valid.
        route("/basic-auth") {
            this@main.authentication {
                basic("ktor-samples-httpbin") {
                    validate {
                        hashedUserTable.authenticate(it)
                    }
                }
            }
            get {
                call.sendHttpBinResponse()
            }
            get("{user}/{password}") {
                call.sendHttpBinResponse()
            }
        }

        // Always generate an unauthorized response.
        get("/hidden-basic-auth/{user}/{password}") {
            call.respond(HttpStatusCode.Unauthorized)
        }

        // Instead of replying with a content at once, uses chunked encoding to send a lorenIpsum [n] times
        // serving a chunk per loren ipsum.
        get("/stream/{n}") {
            val lorenIpsum =
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n"
            val times = call.parameters["n"]!!.toInt()
            call.respondTextWriter {
                repeat(times) {
                    write(lorenIpsum)
                    flush()
                }
            }
        }

        // Responds the request after [n] seconds (where n is between 0 and 10 inclusive)
        get("/delay/{n}") {
            val n = call.parameters["n"]!!.toLong()
            require(n in 0..10) { "Expected a number of seconds between 0 and 10" }
            delay(n)
            call.sendHttpBinResponse()
        }

        // Sends a chunked response of [numbytes] '*' bytes over [duration] seconds with the specified http [code].
        // This will delay each chunk according.
        // time curl --no-buffer "http://127.0.0.1:8080/drip?duration=5&numbytes=5000&code=200"
        get("/drip") {
            val duration = call.parameters["duration"]?.toDoubleOrNull() ?: 2.0
            val numbytes = call.parameters["numbytes"]?.toIntOrNull() ?: (10 * 1024 * 1024)
            val code = call.parameters["code"]?.toIntOrNull() ?: 200
            call.respondTextWriter(status = HttpStatusCode.fromValue(code)) {
                val start = System.currentTimeMillis()
                var now = start
                for (n in 0 until numbytes) {
                    val expected = start + ((n + 1) * duration * 1000).toInt() / numbytes
                    val delay = expected - now
                    if (now <= expected) {
                        flush()
                        delay(delay)
                    }

                    write('*'.code)
                    now = System.currentTimeMillis()
                }
            }
        }

        // Gets a response with [n] random bytes from an insecure random source.
        get("/bytes/{n}") {
            val n = call.parameters["n"]!!.toInt()
            val r = Random()
            val buffer = ByteArray(n) { r.nextInt().toByte() }
            call.respond(buffer)
        }

        // A static route where the 'static' folder is the base.
        static {
            staticBasePackage = "static"

            defaultResource("index.html")
            resource("xml", "sample.xml")
            resource("encoding/utf8", "UTF-8-demo.html")
            resource("html", "moby.html")
            resource("robots.txt")
            resource("forms/post", "forms-post.html")
            resource("postman", "httpbin.postman_collection.json")
            resource("httpbin.js")

            // And for the '/static' path, it will serve the [staticFilesDir].
            route("static") {
                resources("static")
            }
        }

        // Handles all the other non-matched routes returning a 404 not found.
        route("{...}") {
            handle {
                val error = HttpBinError(
                    code = HttpStatusCode.NotFound,
                    request = call.request.local.uri,
                    message = "NOT FOUND"
                )
                call.respond(error)
            }
        }
    }
}

/**
 * This [Route] node registers the [method] route that will change depending on the [ContentType] provided by the client
 * about the content it is going to send.
 *
 * In this case, we support several content types serving different content:
 *
 * - [ContentType.MultiPart.FormData]
 * - [ContentType.Application.FormUrlEncoded]
 * - [ContentType.Application.Json]
 * - Others
 */
fun Route.handleRequestWithBodyFor(method: HttpMethod) {
    contentType(ContentType.MultiPart.FormData) {
        method(method) {
            handle {
                val listFiles = call.receive<MultiPartData>().readAllParts().filterIsInstance<PartData.FileItem>()
                call.sendHttpBinResponse {
                    form = call.receive<Parameters>()
                    files = listFiles.associateBy { part -> part.name ?: "a" }
                }
            }
        }
    }
    contentType(ContentType.Application.FormUrlEncoded) {
        method(method) {
            handle {
                call.sendHttpBinResponse {
                    form = call.receive<Parameters>()
                }
            }
        }
    }
    contentType(ContentType.Application.Json) {
        method(method) {
            handle {
                val type = object : TypeToken<Map<String, Any>>() {}.type
                val content = call.receive<String>()
                val response = HttpBinResponse(
                    data = content,
                    json = gson.fromJson(content, type),
                    parameters = call.request.queryParameters,
                    headers = call.request.headers.toMap()
                )
                call.respond(response)
            }
        }
    }
    method(method) {
        handle {
            call.sendHttpBinResponse {
                data = call.receive<String>()
            }
        }
    }
}
