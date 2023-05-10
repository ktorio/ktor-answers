package io.ktor.answers.db

import io.ktor.answers.plugins.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository {
    private val defaultQueryParams = CommonQueryParams(0, 20, null, null)
    suspend fun allUsers(
        parsed: CommonQueryParams = defaultQueryParams,
        sortBy: String = "name",
        order: SortOrder = ASC
    ): List<User> = asyncTransaction {
        User
            .find {
                val active = UserTable.active eq true
                val from = if (parsed.fromDate != null) (UserTable.createdAt.date() greaterEq parsed.fromDate) else null
                val to = if (parsed.toDate != null) UserTable.createdAt.date() lessEq parsed.toDate else null
                sequenceOf(active, to, from).filterNotNull().reduce { acc, op -> acc and op }
            }
            .apply {
                if (parsed.page != null) limit(parsed.pageSize, parsed.pageSize.toLong() * (parsed.page - 1))
                else limit(parsed.pageSize)
            }
            .orderBy(
                when (sortBy) {
                    "name" -> UserTable.name
                    "creation" -> UserTable.createdAt
                    else -> error("Unsupported sort column: $sortBy")
                } to order
            )
            .toList()
    }

    suspend fun userById(id: Long): User? = asyncTransaction {
        User
            .find { (UserTable.id eq id) and (UserTable.active eq true) }
            .limit(1).firstOrNull()
    }

    suspend fun userAnswers(userId: Long): List<Answer> = asyncTransaction {
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

    suspend fun userComments(userId: Long): List<Comment> = asyncTransaction {
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

    suspend fun userQuestions(userId: Long): List<Question> = asyncTransaction {
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

suspend fun <T> asyncTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
