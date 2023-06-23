package io.ktor.answers.model

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

enum class PostType {
    QUESTION,
    ANSWER,
}

enum class UserType {
    UNREGISTERED,
    REGISTERED,
    MODERATOR,
    TEAM_ADMIN,
    DOES_NOT_EXIST,
}

@Serializable
sealed interface Post {
    val postId: Int
    val postType: PostType
    val creationDate: LocalDateTime
    val lastActivityDate: LocalDateTime
    val lastEditDate: LocalDateTime
    val link: String
    val title: String
    val body: String
    val comments: List<PostComment>
    val upVoteCount: Int
    val downVoteCount: Int
    val owner: User
}

@Serializable
data class Question(
    override val postId: Int,
    override val postType: PostType,
    override val creationDate: LocalDateTime,
    override val lastActivityDate: LocalDateTime,
    override val lastEditDate: LocalDateTime,
    override val link: String,
    override val title: String,
    override val body: String,
    override val comments: List<PostComment>,
    override val upVoteCount: Int,
    override val downVoteCount: Int,
    override val owner: User,
    val isAnswered: Boolean,
    val acceptedAnswerId: Int?,
    val answers: List<Answer>
): Post

@Serializable
data class Answer(
    override val postId: Int,
    override val postType: PostType,
    override val creationDate: LocalDateTime,
    override val lastActivityDate: LocalDateTime,
    override val lastEditDate: LocalDateTime,
    override val link: String,
    override val title: String,
    override val body: String,
    override val comments: List<PostComment>,
    override val upVoteCount: Int,
    override val downVoteCount: Int,
    override val owner: User,
    val accepted: Boolean = false,
    val questionId: Int? = null
) : Post

@Serializable
data class PostComment(
    val commentId: Int,
    val owner: User,
    val postId: Int,
    val body: String
)

@Serializable
data class User(
    val userId: Int,
    val userType: UserType,
    val displayName: String,
    val creationDate: LocalDateTime,
    val link: String,
    val location: String? = null,
    val aboutMe: String? = null
)

// TODO: can we do better?
// This is another class with all the properties nullable for create/update
@Serializable
data class QuestionData(
    val postId: Int? = null,
    val postType: PostType? = null,
    val creationDate: LocalDateTime? = null,
    val lastActivityDate: LocalDateTime? = null,
    val lastEditDate: LocalDateTime? = null,
    val link: String? = null,
    val title: String? = null,
    val body: String? = null,
    val comments: List<PostComment>? = null,
    val upVoteCount: Int? = null,
    val downVoteCount: Int? = null,
    val owner: User? = null,
    val isAnswered: Boolean? = null,
    val acceptedAnswerId: Int? = null,
    val answers: List<Answer>? = null,
)
