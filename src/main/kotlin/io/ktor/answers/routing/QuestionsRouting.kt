package io.ktor.answers.routing

import io.ktor.answers.db.*
import io.ktor.answers.db.fake.model.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.resources.*
import io.ktor.server.resources.post

@Resource("/questions2")
class QuestionsResource {
    @Resource("/{id}")
    class Id(val path: QuestionsResource = QuestionsResource(), val id: Int)
}

@Resource("/users2")
class UsersResource

fun Routing.questionsRouting(questionsRepository: QuestionRepository) {
    get<QuestionsResource> {
        call.respond(questionsRepository.getQuestions())
    }

    get<QuestionsResource.Id> { path ->
        val question = questionsRepository.getQuestionById(path.id)
        if(question != null) {
            call.respond(question)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post<QuestionsResource> {
        val newQuestionData: QuestionData = call.receive()
        val question = questionsRepository.addQuestion(newQuestionData)
        if (question == null) {
            // TODO
            call.respond(HttpStatusCode.PartialContent)
        } else {
            call.respond(question)
        }
    }

    delete<QuestionsResource.Id> { path ->
        if(questionsRepository.deleteQuestionById(path.id)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    get<UsersResource> {
        //call.respond(questionsRepository.getUsers())
        call.respond(HttpStatusCode.NotFound)
    }
}