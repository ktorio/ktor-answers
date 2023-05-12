package io.ktor.answers.routing

import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import org.jetbrains.exposed.sql.SortOrder

data class CommonQueryParams(
    val page: Int?,
    val pageSize: Int,
    val fromDate: LocalDate?,
    val toDate: LocalDate?,
    val sortBy: String?,
    val order: SortOrder,
)

fun Parameters.parsed(defaultSortField: String? = null): CommonQueryParams {
    val page = this["page"]?.toInt()
    val pageSize = this["pagesize"]?.toInt() ?: 20
    val fromDate = this["fromdate"]?.toLocalDate()
    val toDate = this["todate"]?.toLocalDate()
    val sortBy = this["sortby"] ?: defaultSortField
    val order = (this["order"] ?: "asc").toSortOrder()
    return CommonQueryParams(
        page,
        pageSize,
        fromDate,
        toDate,
        sortBy,
        order
    )
}