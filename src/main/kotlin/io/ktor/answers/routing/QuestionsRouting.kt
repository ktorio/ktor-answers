package io.ktor.answers.routing

import io.ktor.answers.fakedb.*
import io.ktor.answers.fakedb.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.questionsRouting(questionsRepository: QuestionsRepository) {
    get("/questions2") {
        call.respond(questionsRepository.getQuestions())
    }
    post("/questions2") {
        val newQuestionData: QuestionData = call.receive()
        val question = questionsRepository.addQuestion(newQuestionData)
        if (question == null) {
            // TODO
            call.respond(HttpStatusCode.PartialContent)
        } else {
            call.respond(question)
        }
    }
    get("/users2") {
        call.respond(questionsRepository.getUsers())
    }
}