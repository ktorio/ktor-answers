package io.ktor.answers

import io.ktor.answers.db.*
import kotlinx.coroutines.awaitAll
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.Scope
import liquibase.command.CommandBuilder
import liquibase.command.CommandFactory
import liquibase.command.CommandResultsBuilder
import liquibase.command.core.UpdateCommandStep
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.*


@Testcontainers
class DbTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres")

        @BeforeAll
        @JvmStatic
        fun init() {
            with(postgres) {

                val database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(DriverManager.getConnection(jdbcUrl, username, password)))
                val liquibase = Liquibase("db/changelog/changelog.xml", ClassLoaderResourceAccessor(), database)
                liquibase.dropAll()
                liquibase.update(Contexts(""), LabelExpression(), true)
                Database.connect(jdbcUrl, driverClassName, username, password)
            }
        }
    }

    @Test
    fun `deactivated users should not be in the 'all users' response`() {

        transaction {
            User.new {
                name = "pasha"
                email = "asm0dey@example.com"
                passwordHash = "***secret***"
                active = false
            }
        }
        assertEquals(0, UserRepository.allUsers().size)
    }
}