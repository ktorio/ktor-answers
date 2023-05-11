package io.ktor.answers.routing

import org.jetbrains.exposed.sql.SortOrder

fun String.toSortOrder(): SortOrder = when (this) {
    "asc" -> SortOrder.ASC
    "desc" -> SortOrder.DESC
    else -> error("Unsupported sort order: $this")
}
