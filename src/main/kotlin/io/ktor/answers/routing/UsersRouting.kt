package io.ktor.answers.routing

import io.ktor.answers.db.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersRouting(userRepository: UserRepository) {
    route("/users") {
        get {
            val queryParams = call.request.queryParameters
            val sortBy = queryParams["sortBy"] ?: "name"
            val order = (queryParams["order"] ?: "asc").toSortOrder()

            call.respond(userRepository.allUsers(queryParams.parsed(), sortBy, order).map(UserDAO::toDTO))
        }
        route(Regex("(?<ids>\\d+(,\\d+){0,1000})")) {
            get {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                val sortBy = queryParams["sortBy"] ?: "name"
                val order = (queryParams["order"] ?: "asc").toSortOrder()
                call.respond(
                    userRepository.usersByIds(ids, queryParams.parsed(), sortBy, order).map { UserDAO::toDTO })
            }
            get("/comments") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                val sortBy = queryParams["sortBy"] ?: "creation"
                val order = (queryParams["order"] ?: "asc").toSortOrder()
                call.respond(userRepository.commentsByIds(ids, queryParams.parsed(), sortBy, order))
            }
            get("/quesions") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                val sortBy = queryParams["sortBy"] ?: "creation"
                val order = (queryParams["order"] ?: "asc").toSortOrder()
                call.respond(userRepository.questionsByIds(ids, queryParams.parsed(), sortBy, order))

            }
            get("/answers") {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                val sortBy = queryParams["sortBy"] ?: "creation"
                val order = (queryParams["order"] ?: "asc").toSortOrder()
                call.respond(userRepository.answersByIds(ids, queryParams.parsed(), sortBy, order))
            }
        }
    }
}
