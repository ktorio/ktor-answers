package io.ktor.answers

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.answers.plugins.*
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager


fun Application.module() {
    val jdbcUrl = environment.config.property("database.url").getString()
    val username = environment.config.property("database.username").getString()
    val password = environment.config.property("database.password").getString()
    val database =
        DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(DriverManager.getConnection(jdbcUrl, username, password)))
    val liquibase = Liquibase("db/changelog/changelog.xml", ClassLoaderResourceAccessor(), database)
    liquibase.dropAll()
    liquibase.update(Contexts(""), LabelExpression(), true)

    configureSerialization()
    configureRouting()
}
