package io.ktor.answers.routing

import io.ktor.answers.fakedb.*
import io.ktor.answers.fakedb.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.locations.post
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/questions2")
object QuestionsPath {
    @Location("/{id}")
    data class ById(val path: QuestionsPath = QuestionsPath, val id: Int)
}

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/users2")
class UsersPath


@OptIn(KtorExperimentalLocationsAPI::class)
fun Routing.questionsRouting(questionsRepository: QuestionsRepository) {
    get<QuestionsPath> {
        call.respond(questionsRepository.getQuestions())
    }

    get<QuestionsPath.ById> { path ->
        val question = questionsRepository.getQuestionById(path.id)
        if(question != null) {
            call.respond(question)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    post<QuestionsPath> {
        val newQuestionData: QuestionData = call.receive()
        val question = questionsRepository.addQuestion(newQuestionData)
        if (question == null) {
            // TODO
            call.respond(HttpStatusCode.PartialContent)
        } else {
            call.respond(question)
        }
    }

    delete<QuestionsPath.ById> { path ->
        if(questionsRepository.deleteQuestionById(path.id)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    get<UsersPath> {
        call.respond(questionsRepository.getUsers())
    }
}