package io.ktor.answers.db.fake

import io.ktor.answers.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileInputStream
import java.util.*

class FakeUserRepository {
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

    fun getUsers(): List<User> {
        return questions
            .flatMap { question ->
                question.answers.map { it.owner } + question.owner
            }
            .distinct()
    }
}
