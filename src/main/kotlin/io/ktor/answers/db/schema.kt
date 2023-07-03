package io.ktor.answers.db

import kotlinx.datetime.Clock
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : LongIdTable("users") {
    val name = varchar("name", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 100)
    val active = bool("active").default(false)
    val email = text("email").uniqueIndex()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
    val displayName = varchar("display_name", 80).uniqueIndex()
    val location = text("location").nullable()
    val aboutMe = text("about_me").nullable()
    val link = text("link").nullable()
}

object RoleTable : IntIdTable("role") {
    val name = varchar("name", 100).uniqueIndex()
}

object UserRole : Table("user_role") {
    val user = reference("user", UserTable, onDelete = ReferenceOption.CASCADE).index()
    val role = reference("role", RoleTable, onDelete = ReferenceOption.CASCADE).index()
    override val primaryKey = PrimaryKey(user, role)
}


object ContentTable : LongIdTable("content") {
    val text = text("text")
    val author = reference("author_id", UserTable, onDelete = ReferenceOption.CASCADE).index()
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }

}

object VoteTable : LongIdTable("vote") {
    val voter = reference("voter", UserTable, onDelete = ReferenceOption.CASCADE).index()
    val content = reference("content", ContentTable, onDelete = ReferenceOption.CASCADE).index()
    val value = short("value")
    val createdAt = timestamp("created_at").clientDefault { Clock.System.now() }
}

object QuestionTable : LongIdTable("question") {
    val data = reference("content", ContentTable, onDelete = ReferenceOption.CASCADE).index()
    val title = text("title").uniqueIndex()
}

object AnswerTable : LongIdTable("answer") {
    val question = reference("question", QuestionTable, onDelete = ReferenceOption.CASCADE).index()
    val data = reference("data", ContentTable, onDelete = ReferenceOption.CASCADE)
    val accepted = bool("accepted").default(false)
}

object CommentTable : LongIdTable("comment") {
    val data = reference("data", ContentTable, onDelete = ReferenceOption.CASCADE).index()
    val parent = reference("parent", ContentTable, onDelete = ReferenceOption.CASCADE).index()
}


object TagTable : LongIdTable("tag") {
    val name = varchar("name", 50).uniqueIndex()
}

object QuestionTags : Table("question_tag") {
    val question = reference("question_id", QuestionTable, onDelete = ReferenceOption.CASCADE).index()
    val tag = reference("tag_id", TagTable, onDelete = ReferenceOption.CASCADE).index()
    override val primaryKey: PrimaryKey = PrimaryKey(question, tag)
}