package io.ktor.answers.routing

import io.ktor.answers.db.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Routing.usersRouting(userRepository: UserRepository) {
    route("/users") {
        get {
            call.respond(userRepository.allUsers(querySortedBy("name")))
        }
        route(Regex("(?<ids>\\d+(,\\d+){0,1000})")) {
            get {
                call.respond(userRepository.usersByIds(ids, querySortedBy("name")))
            }
            get("/comments") {
                call.respond(userRepository.commentsByIds(ids, querySortedBy("creation")))
            }
            get("/quesions") {
                call.respond(userRepository.questionsByIds(ids, querySortedBy("creation")))
            }
            get("/answers") {
                call.respond(userRepository.answersByIds(ids, querySortedBy("creation")))
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.querySortedBy(sortField: String) =
    call.request.queryParameters.parsed(sortField)


private val PipelineContext<Unit, ApplicationCall>.ids
    get() = call.parameters["ids"]!!.split(',').map(String::toLong)
