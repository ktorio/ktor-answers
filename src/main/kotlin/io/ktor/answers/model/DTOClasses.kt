package io.ktor.answers.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long,
    val name: String,
    val active: Boolean = true,
    val email: String,
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val displayName: String,
    val location: String? = null,
    val aboutMe: String? = null,
    val link: String? = null,
)

@Serializable
data class Answer(
    val id: Long,
    val text: String,
    val createdAt: LocalDateTime,
    val authorId: Long,
    val votes: Int
)

@Serializable
data class Question(
    val id: Long,
    val title: String,
    val text: String,
    val createdAt: LocalDateTime,
    val authorId: Long,
    val votes: Int
)

@Serializable
data class Comment(
    val value: Long,
    val text: String,
    val createdAt: LocalDateTime,
    val authorId: Long,
    val votes: Int
)