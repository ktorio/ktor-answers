package io.ktor.answers.db

import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.time.LocalDateTime

object UserTable : LongIdTable("users") {
    val name = varchar("name", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val active = bool("active").default(false)
    val email = text("email").uniqueIndex()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }
}

object RoleTable : IntIdTable("role") {
    val name = varchar("name", 100).uniqueIndex()
}

object UserRole : Table("user_role") {
    val user = reference("user", UserTable, onDelete = ReferenceOption.CASCADE)
    val role = reference("role", RoleTable, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(user, role)
}


object ContentTable : LongIdTable("content") {
    val text = text("text")
    val author = reference("author_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }

}

object VoteTable : LongIdTable("vote") {
    val voter = reference("voter", UserTable, onDelete = ReferenceOption.CASCADE)
    val content = reference("content", ContentTable, onDelete = ReferenceOption.CASCADE)
    val value = short("value")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }
}

object QuestionTable : LongIdTable("question") {
    val data = reference("content", ContentTable, onDelete = ReferenceOption.CASCADE)
    val title = text("title").uniqueIndex()
}

object AnswerTable : LongIdTable("answer") {
    val question = reference("question", QuestionTable, onDelete = ReferenceOption.CASCADE)
    val data = reference("data", ContentTable, onDelete = ReferenceOption.CASCADE)
}

object CommentTable : LongIdTable("comment") {
    val data = reference("data", ContentTable, onDelete = ReferenceOption.CASCADE)
    val parent = reference("parent", ContentTable, onDelete = ReferenceOption.CASCADE)
}


object TagTable : LongIdTable("tag") {
    val name = varchar("name", 50).uniqueIndex()
}

object QuestionTags : Table("question_tag") {
    val question = reference("question_id", QuestionTable, onDelete = ReferenceOption.CASCADE)
    val tag = reference("tag_id", TagTable, onDelete = ReferenceOption.CASCADE)
    override val primaryKey: PrimaryKey = PrimaryKey(question, tag)
}