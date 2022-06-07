package io.ktor.samples

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*

fun Application.h2(): HikariDataSource {
    val config = HikariConfig().apply {
        driverClassName = "org.h2.Driver"
        jdbcUrl = "jdbc:h2:mem:myDb;DB_CLOSE_DELAY=-1"
    }

    return HikariDataSource(config)
}

private fun HikariConfig.readH2ConfigFromProperties(properties: ApplicationConfig) {
    properties.propertyOrNull("jdbcUrl")?.let { jdbcUrl = it.getString() }
    properties.propertyOrNull("username")?.let { username = it.getString() }
    properties.propertyOrNull("password")?.let { password = it.getString() }
}
