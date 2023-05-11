package io.ktor.answers.routing

import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

data class CommonQueryParams(
    val page: Int?,
    val pageSize: Int,
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
)

fun Parameters.parsed(): CommonQueryParams {
    val page = this["page"]?.toInt()
    val pageSize = this["pagesize"]?.toInt() ?: 20
    val fromDate = this["fromdate"]?.toLocalDate()
    val toDate = this["todate"]?.toLocalDate()
    return CommonQueryParams(page, pageSize, fromDate, toDate)
}