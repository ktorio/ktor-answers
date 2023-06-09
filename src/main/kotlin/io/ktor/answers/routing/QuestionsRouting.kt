package io.ktor.answers.routing

import io.ktor.answers.fakedb.*
import io.ktor.answers.fakedb.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.questionsRouting(questionsRepository: QuestionsRepository) {
    get("/questions") {
        call.respond(questionsRepository.getQuestions())
    }

    get("/questions/{id}") {
        //this could be a case for a route scoped validation plugin
        val ids: String = call.parameters["id"]!!

        val id = try { ids.toInt() } catch (e: NumberFormatException) {
            call.respond(HttpStatusCode.BadRequest, "Incorrect id: $ids")
            return@get
        }

        val question = questionsRepository.getQuestionById(id)

        if (question != null) {
            call.respond(question)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post("/questions") {
        val newQuestionData: QuestionData = call.receive()
        val question = questionsRepository.addQuestion(newQuestionData)
        if (question == null) {
            call.respond(HttpStatusCode.PartialContent)
        } else {
            call.respond(question)
        }
    }

    delete("/questions/{id}") {
        val ids: String = call.parameters["id"]!!

        val id = try { ids.toInt() } catch (e: NumberFormatException) {
            call.respond(HttpStatusCode.BadRequest, "Incorrect id: $ids")
            return@delete
        }

        if(questionsRepository.deleteQuestionById(id)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
