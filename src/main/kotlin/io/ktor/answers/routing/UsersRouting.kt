package io.ktor.answers.routing

import io.ktor.answers.db.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersRouting(userRepository: UserRepository) {
    route("/users") {
        get {
            val queryParams = call.request.queryParameters
            call.respond(userRepository.allUsers(queryParams.parsed(defaultSortField = "name")).map(UserDAO::toDTO))
        }
        route(Regex("(?<ids>\\d+(,\\d+){0,1000})")) {
            get {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                call.respond(
                    userRepository.usersByIds(ids, queryParams.parsed(defaultSortField = "name"))
                        .map { UserDAO::toDTO })
            }
            get("/comments") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                call.respond(userRepository.commentsByIds(ids, queryParams.parsed(defaultSortField = "creation")))
            }
            get("/questions") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                call.respond(userRepository.questionsByIds(ids, queryParams.parsed(defaultSortField = "creation")))

            }
            get("/answers") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                call.respond(userRepository.answersByIds(ids, queryParams.parsed(defaultSortField = "creation")))
            }
        }
    }
}
