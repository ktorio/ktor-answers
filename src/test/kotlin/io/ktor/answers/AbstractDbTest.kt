package io.ktor.answers

import org.jetbrains.exposed.sql.Database
import org.testcontainers.containers.PostgreSQLContainer

abstract class AbstractDbTest {
    companion object {
        @JvmField
        var postgres = PostgreSQLContainer("postgres")

        init {
            postgres.start()
            val url = postgres.jdbcUrl
            val user = postgres.username
            val password = postgres.password
            migrate(url, user, password)
            Database.connect(url, user = user, password = password)
        }
    }
}