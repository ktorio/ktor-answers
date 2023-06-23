package io.ktor.answers.routing

import io.ktor.answers.db.fake.*
import io.ktor.answers.db.postgres.*
import io.ktor.http.*
import io.ktor.server.resources.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
    }
    install(Resources)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        val userRepository = UserRepository() // TODO DI
        usersRouting(userRepository)
        //val questionsRepository = FakeQuestionRepository()
        val questionsRepository = PostgresQuestionRepository()
        questionsRouting(questionsRepository)
    }
}
