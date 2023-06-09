package io.ktor.answers.routing

import io.ktor.answers.db.postgres.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.usersRouting(userRepository: PostgresUserRepository) {
    route("/users") {
        get {
            val queryParams = call.request.queryParameters
            val users = userRepository.allUsers(queryParams.parsed(defaultSortField = "name"))
            call.respond(users.map(::daoToDto))
        }
        route(Regex("(?<ids>\\d+(,\\d+){0,1000})")) {
            get {
                val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                val queryParams = call.request.queryParameters
                val users = userRepository.usersByIds(ids, queryParams.parsed(defaultSortField = "name"))
                call.respond(users.map(::daoToDto))
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
