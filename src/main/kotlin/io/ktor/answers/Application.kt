package io.ktor.answers

import io.ktor.answers.plugins.*
import io.ktor.answers.routing.*
import io.ktor.server.application.*
import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep.CHANGELOG_FILE_ARG
import liquibase.command.core.UpdateCommandStep.COMMAND_NAME
import liquibase.command.core.helpers.DbUrlConnectionCommandStep.DATABASE_ARG
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import org.jetbrains.exposed.sql.Database
import java.sql.DriverManager

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    migrateDb()
    connectToDb()
    configureSerialization()
    configureRouting()
}

fun Application.connectToDb() = with(environment.config){
    Database.connect(
        property("database.url").getString(),
        user = property("database.username").getString(),
        password = property("database.password").getString()
    )
}

fun Application.migrateDb() = with(environment.config) {
    migrate(
        property("database.url").getString(),
        property("database.username").getString(),
        property("database.password").getString()
    )
}

fun migrate(jdbcUrl: String, username: String, password: String) {
    Scope.child(mapOf()) {
        val database =
            DatabaseFactory
                .getInstance()
                .findCorrectDatabaseImplementation(
                    JdbcConnection(DriverManager.getConnection(jdbcUrl, username, password))
                )
        CommandScope(*COMMAND_NAME).apply {
            addArgumentValue(DATABASE_ARG, database)
            addArgumentValue(CHANGELOG_FILE_ARG, "db/changelog/changelog.xml")
            execute()
        }
    }
}
