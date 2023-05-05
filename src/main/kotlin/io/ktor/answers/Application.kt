package io.ktor.answers

import io.ktor.answers.plugins.*
import io.ktor.server.application.*
import liquibase.Liquibase
import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep.CHANGELOG_FILE_ARG
import liquibase.command.core.UpdateCommandStep.COMMAND_NAME
import liquibase.command.core.helpers.DbUrlConnectionCommandStep.DATABASE_ARG
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager


fun Application.module() {
    migrateDb()
    configureSerialization()
    configureRouting()
}

fun Application.migrateDb() = with(environment.config) {
    migrate(
        property("database.url").getString(),
        property("database.username").getString(),
        property("database.password").getString()
    )
}

fun migrate(jdbcUrl: String, username: String, password: String, drop: Boolean = false) {
    Scope.child(mapOf()) {
        val database =
            DatabaseFactory
                .getInstance()
                .findCorrectDatabaseImplementation(
                    JdbcConnection(DriverManager.getConnection(jdbcUrl, username, password))
                )
        val liquibase = Liquibase("db/changelog/changelog.xml", ClassLoaderResourceAccessor(), database)
        if (drop)
            liquibase.dropAll()
        CommandScope(*COMMAND_NAME).apply {
            addArgumentValue(DATABASE_ARG, database)
            addArgumentValue(CHANGELOG_FILE_ARG, "db/changelog/changelog.xml")
            execute()
        }
    }
}
