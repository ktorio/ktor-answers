package io.ktor.answers.db.fake

import io.ktor.answers.db.*
import io.ktor.answers.db.fake.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
import java.util.*

class FakeQuestionRepository : QuestionRepository {
    private val questions: MutableList<Question> =
        Collections.synchronizedList(mutableListOf())

    init {
        val json = Json {
            ignoreUnknownKeys = true
        }

        val file = File("src/main/resources/fakeDB.json")
        val inputStream = FileInputStream(file)

        val fakeData: FakeData = json.decodeFromStream(inputStream)
        questions.addAll(fakeData.questions)
    }

    override fun getQuestions(): List<Question> {
        return questions
    }

    override fun getQuestionById(id: Int): Question? {
        return questions.find { it.postId == id }
    }

    override fun deleteQuestionById(id: Int): Boolean {
        return questions.removeIf { it.postId == id }
    }

    override fun getUsers(): List<User> {
        return questions
            .flatMap { question ->
                question.answers.map { it.owner } + question.owner
            }
            .distinct()
    }

    override fun addQuestion(newQuestionData: QuestionData): Question? {
        val maxId = questions.maxOf { it.postId }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val question = Question(
            maxId + 1,
            postType = newQuestionData.postType ?: PostType.QUESTION,
            title = newQuestionData.title ?: return null,
            body = newQuestionData.body ?: return null,
            creationDate = newQuestionData.creationDate ?: now,
            lastActivityDate = newQuestionData.creationDate ?: now,
            lastEditDate = newQuestionData.creationDate ?: now,
            link = newQuestionData.link ?: "",
            comments = newQuestionData.comments ?: listOf(),
            upVoteCount = newQuestionData.upVoteCount ?: 0,
            downVoteCount = newQuestionData.downVoteCount ?: 0,
            owner = newQuestionData.owner ?: return null,
            isAnswered = newQuestionData.isAnswered ?: false,
            acceptedAnswerId = newQuestionData.acceptedAnswerId,
            answers = newQuestionData.answers ?: listOf(),
        )
        questions.add(question)
        return question
    }
}

@Serializable
data class FakeData(val questions: List<Question>)
