package io.ktor.answers.plugins

import io.ktor.answers.db.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.jetbrains.exposed.sql.SortOrder

fun Application.configureRouting() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        route("/users") {
            get {
                val queryParams = call.request.queryParameters
                val sortBy = queryParams["sortBy"] ?: "name"
                val order = (queryParams["order"] ?: "asc").toSortOrder()

                call.respond(UserRepository().allUsers(queryParams.parsed(), sortBy, order).map(UserDAO::toDTO))
            }
            route(Regex("(?<ids>\\d+(,\\d+){0,1000})")) {
                get {
                    val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                    val queryParams = call.request.queryParameters
                    val sortBy = queryParams["sortBy"] ?: "name"
                    val order = (queryParams["order"] ?: "asc").toSortOrder()
                    call.respond(UserRepository().usersByIds(ids, queryParams.parsed(), sortBy, order).map { UserDAO::toDTO })
                }
                get("/comments"){
                    val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                    val queryParams = call.request.queryParameters
                    val sortBy = queryParams["sortBy"] ?: "creation"
                    val order = (queryParams["order"] ?: "asc").toSortOrder()
                    call.respond(UserRepository().commentsByIds(ids, queryParams.parsed(), sortBy, order))
                }
                get("/quesions"){
                    val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                    val queryParams = call.request.queryParameters
                    val sortBy = queryParams["sortBy"] ?: "creation"
                    val order = (queryParams["order"] ?: "asc").toSortOrder()
                    call.respond(UserRepository().questionsByIds(ids, queryParams.parsed(), sortBy, order))

                }
                get("/answers"){
                    val ids = call.parameters["ids"]!!.split(',').map(String::toLong)
                    val queryParams = call.request.queryParameters
                    val sortBy = queryParams["sortBy"] ?: "creation"
                    val order = (queryParams["order"] ?: "asc").toSortOrder()
                    call.respond(UserRepository().answersByIds(ids, queryParams.parsed(), sortBy, order))
                }
            }
        }
    }
}

private fun String.toSortOrder(): SortOrder = when (this) {
    "asc" -> SortOrder.ASC
    "desc" -> SortOrder.DESC
    else -> error("Unsupported sort order: $this")
}

private fun Parameters.parsed(): CommonQueryParams {
    val page = this["page"]?.toInt()
    val pageSize = this["pagesize"]?.toInt() ?: 20
    val fromDate = this["fromdate"]?.toLocalDate()
    val toDate = this["todate"]?.toLocalDate()
    return CommonQueryParams(page, pageSize, fromDate, toDate)
}

data class CommonQueryParams(
    val page: Int?,
    val pageSize: Int,
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
)

