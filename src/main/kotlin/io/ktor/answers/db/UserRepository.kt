package io.ktor.answers.db

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {
    fun allUsers(): List<User> {
        return transaction { User.find { UserTable.active eq true }.notForUpdate().toList() }
    }

    fun userById(id: Long): User? = transaction {
        User
            .find { (UserTable.id eq id) and (UserTable.active eq true) }
            .limit(1).firstOrNull()
    }

    fun userAnswers(userId: Long) = transaction {
        Answer
            .wrapRows(
                UserTable
                    .innerJoin(ContentTable)
                    .innerJoin(AnswerTable)
                    .slice(AnswerTable.columns)
                    .select { UserTable.id eq userId and (UserTable.active eq true) }
            )
            .toList()
    }

    fun userComments(userId: Long) = transaction {
        Comment
            .wrapRows(
                CommentTable
                    .innerJoin(ContentTable)
                    .innerJoin(AnswerTable)
                    .slice(AnswerTable.columns)
                    .select { UserTable.id eq userId and (UserTable.active eq true) }
            )
            .toList()
    }

    fun userQuestions(userId: Long) = transaction {
        Question
            .wrapRows(
                QuestionTable
                    .innerJoin(ContentTable)
                    .innerJoin(AnswerTable)
                    .slice(AnswerTable.columns)
                    .select { UserTable.id eq userId and (UserTable.active eq true) }
            )
            .toList()
    }
}

