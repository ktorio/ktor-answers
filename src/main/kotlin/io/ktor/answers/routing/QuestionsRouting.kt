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
    get("/questions2/{id}") {
        val id = call.parameters["id"]
        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "No ID")
            return@get
        }
        try {
            val question = questionsRepository.getQuestionById(id.toInt())
            if(question != null) {
                call.respond(question)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch(e: NumberFormatException) {
            call.respond(HttpStatusCode.BadRequest, "'$id' is not an integer")
        }
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
    delete("/questions2/{id}") {
        val id = call.parameters["id"]
        if(id == null) {
            call.respond(HttpStatusCode.BadRequest, "No ID")
            return@delete
        }
        try {
            if(questionsRepository.deleteQuestionById(id.toInt())) {
                //call.respondText("Question '$id' deleted")
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        } catch(e: NumberFormatException) {
            call.respond(HttpStatusCode.BadRequest, "'$id' is not an integer")
        }
    }
    get("/users2") {
        call.respond(questionsRepository.getUsers())
    }
}