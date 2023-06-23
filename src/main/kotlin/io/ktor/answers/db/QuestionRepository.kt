package io.ktor.answers.db

import io.ktor.answers.db.fake.model.*

interface QuestionRepository {
    suspend fun getQuestions(): List<Question>
    suspend fun getQuestionById(id: Int): Question?
    suspend fun deleteQuestionById(id: Int): Boolean
    suspend fun addQuestion(newQuestionData: QuestionData): Question?
}