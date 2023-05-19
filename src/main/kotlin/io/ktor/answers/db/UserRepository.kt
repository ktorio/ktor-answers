package io.ktor.answers.db

import io.ktor.answers.model.*
import io.ktor.answers.plugins.*
import io.ktor.answers.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.JoinType.INNER
import org.jetbrains.exposed.sql.SortOrder.ASC
import org.jetbrains.exposed.sql.SqlExpressionBuilder.coalesce
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq

val defaultQueryParams = CommonQueryParams(0, 20, null, null, null, ASC)

class UserRepository {
    suspend fun allUsers(
        parsed: CommonQueryParams = defaultQueryParams,
    ): List<UserDAO> = suspendTransaction {
        UserDAO
            .find {
                val active = UserTable.active eq true
                val from = if (parsed.fromDate != null) (UserTable.createdAt.date() greaterEq parsed.fromDate) else null
                val to = if (parsed.toDate != null) UserTable.createdAt.date() lessEq parsed.toDate else null
                sequenceOf(active, to, from).filterNotNull().reduce(Op<Boolean>::and)
            }
            .limit(parsed.pageSize, if (parsed.page != null) parsed.pageSize.toLong() * (parsed.page - 1) else 0)
            .orderBy(
                when (parsed.sortBy ?: "name") {
                    "name" -> UserTable.name
                    "creation" -> UserTable.createdAt
                    else -> error("Unsupported sort column: ${parsed.sortBy}")
                } to parsed.order
            )
            .toList()
    }

    suspend fun userById(id: Long): UserDAO? = suspendTransaction {
        UserDAO
            .find { (UserTable.id eq id) and (UserTable.active eq true) }
            .limit(1).firstOrNull()
    }

    suspend fun usersByIds(
        ids: List<Long>,
        queryParams: CommonQueryParams = defaultQueryParams,
    ): List<UserDAO> = suspendTransaction {
        UserDAO
            .find {
                val active = UserTable.active eq true
                val inIds = UserTable.id inList ids
                val from =
                    if (queryParams.fromDate != null) (UserTable.createdAt.date() greaterEq queryParams.fromDate) else null
                val to = if (queryParams.toDate != null) UserTable.createdAt.date() lessEq queryParams.toDate else null
                sequenceOf(active, inIds, to, from).filterNotNull().reduce(Op<Boolean>::and)
            }
            .limit(
                queryParams.pageSize,
                if (queryParams.page != null) queryParams.pageSize.toLong() * (queryParams.page - 1) else 0
            )
            .orderBy(
                when (queryParams.sortBy ?: "name") {
                    "name" -> UserTable.name
                    "creation" -> UserTable.createdAt
                    else -> error("Unsupported sort column: ${queryParams.sortBy}")
                } to queryParams.order
            )
            .toList()

    }

    suspend fun commentsByIds(
        ids: List<Long>,
        queryParams: CommonQueryParams = defaultQueryParams,
    ): List<CommentDTO> = suspendTransaction {
        val text = ContentTable.text.min().alias("comment_text")
        val createdAt = ContentTable.createdAt.min().alias("comment_created")
        val author = ContentTable.author.min().alias("author")
        val votes = coalesce(VoteTable.value.sum(), shortLiteral(0)).alias("votes")
        CommentTable
            .join(ContentTable, INNER, CommentTable.data, ContentTable.id)
            .join(UserTable, INNER, ContentTable.author, UserTable.id)
            .join(VoteTable, INNER, VoteTable.content, ContentTable.id)
            .slice(CommentTable.id, text, createdAt, author, votes)
            .select { contentFilters(ids, queryParams.fromDate, queryParams.toDate) }
            .groupBy(CommentTable.id)
            .limit(
                queryParams.pageSize,
                if (queryParams.page != null) queryParams.pageSize.toLong() * (queryParams.page - 1) else 0
            )
            .orderBy(
                when (queryParams.sortBy ?: "creation") {
                    "creation" -> createdAt
                    "votes" -> votes
                    else -> error("Unsupported sort predicate: ${queryParams.sortBy}")
                }, queryParams.order
            )
            .map {
                CommentDTO(
                    it[CommentTable.id].value,
                    it[text]!!,
                    it[createdAt]!!,
                    it[author]!!.value,
                    it[votes].toInt()
                )
            }

    }

    suspend fun questionsByIds(
        ids: List<Long>,
        queryParams: CommonQueryParams = defaultQueryParams,
    ): List<QuestionDTO> = suspendTransaction {
        val text = ContentTable.text.min().alias("question_text")
        val createdAt = ContentTable.createdAt.min().alias("question_created")
        val author = ContentTable.author.min().alias("author")
        val votes = Coalesce(VoteTable.value.sum(), shortLiteral(0)).alias("votes")
        val questionTitle = QuestionTable.title.min().alias("question_title")
        QuestionTable
            .join(ContentTable, INNER, QuestionTable.data, ContentTable.id)
            .join(UserTable, INNER, ContentTable.author, UserTable.id)
            .join(VoteTable, INNER, VoteTable.content, ContentTable.id)
            .slice(QuestionTable.id, questionTitle, text, createdAt, author, votes)
            .select { contentFilters(ids, queryParams.fromDate, queryParams.toDate) }
            .groupBy(QuestionTable.id)
            .limit(
                queryParams.pageSize,
                if (queryParams.page != null) queryParams.pageSize.toLong() * (queryParams.page - 1) else 0
            )
            .orderBy(
                when (queryParams.sortBy ?: "title") {
                    "creation" -> createdAt
                    "votes" -> votes
                    "title" -> questionTitle
                    else -> error("Unsupported sort predicate: ${queryParams.sortBy}")
                }, queryParams.order
            )
            .map {
                QuestionDTO(
                    it[QuestionTable.id].value,
                    it[questionTitle]!!,
                    it[text]!!,
                    it[createdAt]!!,
                    it[author]!!.value,
                    it[votes].toInt()
                )
            }

    }

    suspend fun answersByIds(
        ids: List<Long>,
        queryParams: CommonQueryParams = defaultQueryParams,
    ): List<AnswerDTO> = suspendTransaction {
        val text = ContentTable.text.min().alias("question_text")
        val createdAt = ContentTable.createdAt.min().alias("question_created")
        val author = ContentTable.author.min().alias("author")
        val votes = Coalesce(VoteTable.value.sum(), shortLiteral(0)).alias("votes")
        AnswerTable
            .join(ContentTable, INNER, AnswerTable.data, ContentTable.id)
            .join(UserTable, INNER, ContentTable.author, UserTable.id)
            .join(VoteTable, INNER, VoteTable.content, ContentTable.id)
            .slice(AnswerTable.id, text, createdAt, author, votes)
            .select { contentFilters(ids, queryParams.fromDate, queryParams.toDate) }
            .groupBy(AnswerTable.id)
            .limit(
                queryParams.pageSize,
                if (queryParams.page != null) queryParams.pageSize.toLong() * (queryParams.page - 1) else 0
            )
            .orderBy(
                when (queryParams.sortBy ?: "creation") {
                    "creation" -> createdAt
                    "votes" -> votes
                    else -> error("Unsupported sort predicate: ${queryParams.sortBy}")
                }, queryParams.order
            )
            .map {
                AnswerDTO(
                    it[AnswerTable.id].value,
                    it[text]!!,
                    it[createdAt]!!,
                    it[author]!!.value,
                    it[votes].toInt()
                )
            }

    }

    private fun contentFilters(
        userIds: List<Long>,
        fromDate: LocalDate?,
        toDate: LocalDate?
    ): Op<Boolean> {
        val inIds = UserTable.id inList userIds
        val active = UserTable.active eq true
        val from =
            if (fromDate != null) (ContentTable.createdAt.date() greaterEq fromDate) else null
        val to =
            if (toDate != null) ContentTable.createdAt.date() lessEq toDate else null
        return listOfNotNull(inIds, active, from, to).compoundAnd()
    }
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)
