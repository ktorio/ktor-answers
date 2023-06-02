package io.ktor.answers

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import io.ktor.answers.plugins.*
import io.ktor.answers.routing.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class UsersSingleServerTest : AbstractDbTest() {
    companion object {
        private val testApp = TestApplication {
            environment {
                config = MapApplicationConfig(
                    "database.url" to postgres.jdbcUrl,
                    "database.username" to postgres.username,
                    "database.password" to postgres.password
                )
            }
            application {
                migrateDb()
                configureSerialization()
                configureRouting()
            }
        }
        private val client = testApp.client

        @JvmStatic
        @BeforeAll
        fun start(): Unit = testApp.start()

        @JvmStatic
        @AfterAll
        fun end(): Unit = testApp.stop()
    }

    @Test
    fun shouldFindThreeUsers() = runBlocking {
        val jsonDoc = client.getAsJsonPath("/users")
        assertEquals(3, jsonDoc.read("$.length()"))
    }

    @Test
    fun shouldBeNamedCorrectly() = runBlocking {
        val jsonDoc = client.getAsJsonPath("/users")
        val result: List<String> = jsonDoc.read("$[*].name")
        assertEquals("Demorrio Wyble", result[0])
        assertEquals("Kongmeng Said", result[1])
        assertEquals("Synthia Olmos", result[2])
    }

    @Test
    fun shouldHaveTheCorrectLocation() = runBlocking {
        val jsonDoc = client.getAsJsonPath("/users")
        val locationOfUser1: List<String> = jsonDoc.read("$[?(@.id == 1)].location")
        val locationOfUser2: List<String> = jsonDoc.read("$[?(@.id == 2)].location")
        val locationOfUser3: List<String> = jsonDoc.read("$[?(@.id == 3)].location")
        assertEquals("Copperopolis", locationOfUser1[0])
        assertEquals("Melvin Village", locationOfUser2[0])
        assertEquals("Launceston", locationOfUser3[0])
    }

    @Test
    fun shouldAlwaysHaveAnEmail() = runBlocking {
        val jsonDoc = client.getAsJsonPath("/users")
        assertEquals(3, jsonDoc.read<List<*>>("$[?(@.email)]").size)
    }

    @Test
    fun shouldAlwaysUseSecureLinks() = runBlocking {
        val jsonDoc = client.getAsJsonPath("/users")
        val result: List<Int> = jsonDoc.read("$[?(@.link =~ /https:.*/)].id")
        (1..3).forEach {
            assertContains(result, it)
        }
    }

    suspend fun HttpClient.getAsJsonPath(url: String): DocumentContext {
        val response = this.get(url) {
            accept(ContentType.Application.Json)
        }
        return JsonPath.parse(response.bodyAsText())
    }
}