package io.ktor.answers

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import io.ktor.answers.routing.*
import io.ktor.server.config.*

class ApplicationTest : AbstractDbTest() {
    @Test
    fun testRoot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "database.url" to postgres.jdbcUrl,
                "database.username" to postgres.username,
                "database.password" to postgres.password
            )
        }
        application {
            migrateDb()
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
}
