package io.ktor.answers.db

import io.ktor.answers.db.fake.model.*

interface QuestionRepository {
    fun getQuestions(): List<Question>
    fun getQuestionById(id: Int): Question?
    fun deleteQuestionById(id: Int): Boolean
    fun getUsers(): List<User>
    fun addQuestion(newQuestionData: QuestionData): Question?
}