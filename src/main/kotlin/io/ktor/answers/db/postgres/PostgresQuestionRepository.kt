package io.ktor.answers.db.postgres

import io.ktor.answers.db.*
import io.ktor.answers.db.fake.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun rightNow() = Clock.System.now().toLocalDateTime(TimeZone.UTC)

fun daoToDto(dao: UserDAO): User {
    return User(
        userId = dao.id.value.toInt(),
        userType = UserType.REGISTERED,
        displayName = dao.displayName,
        creationDate = dao.createdAt,
        link = dao.link ?: "",
        location = dao.location ?: "",
        aboutMe = dao.aboutMe ?: ""
    )
}

fun daoToDto(dao: AnswerDAO, questionID: Int): Answer = Answer(
    postId = dao.id.value.toInt(),
    postType = PostType.ANSWER,
    creationDate = dao.data.createdAt,
    lastActivityDate = rightNow(),
    lastEditDate = rightNow(),
    title = "",
    body = dao.data.text,
    comments = emptyList(),
    upVoteCount = 0,
    downVoteCount = 0,
    owner = daoToDto(dao.data.author),
    accepted = dao.accepted,
    questionId = questionID,
    link = "http://www.nowhere.com"
)

fun daoToDto(dao: QuestionDAO): Question {
    val id = dao.id.value.toInt()

    return Question(
        postId = id,
        postType = PostType.QUESTION,
        creationDate = dao.data.createdAt,
        lastActivityDate = rightNow(),
        lastEditDate = rightNow(),
        title = dao.title,
        body = dao.data.text,
        comments = emptyList(),
        upVoteCount = 0,
        downVoteCount = 0,
        owner = daoToDto(dao.data.author),
        isAnswered = false,
        acceptedAnswerId = null,
        answers = dao.answers.map { answer ->
            daoToDto(answer, id)
        }.toList(),
        link = "http://www.nowhere.com"
    )
}

fun dataToDto(data: QuestionData): QuestionDAO {
    val id = data.postId

    return QuestionDAO.new(id?.toLong()) {
    }
}

class PostgresQuestionRepository : QuestionRepository {
    override suspend fun getQuestions(): List<Question> = suspendTransaction {
        QuestionDAO.all()
            .map { daoToDto(it) }
            .toList()
    }

    override suspend fun getQuestionById(id: Int): Question? = suspendTransaction {
        QuestionDAO
            .find { QuestionTable.id eq id.toLong() }
            .map(::daoToDto)
            .firstOrNull()
    }

    override suspend fun deleteQuestionById(id: Int): Boolean = suspendTransaction {
        val question = QuestionDAO
            .find { QuestionTable.id eq id.toLong() }
            .firstOrNull()

        if(question == null) {
            false
        } else {
            question.delete()
            true
        }
    }

    override suspend fun addQuestion(data: QuestionData): Question? = suspendTransaction {
        val dao = dataToDto(data)
        daoToDto(dao)
    }
}