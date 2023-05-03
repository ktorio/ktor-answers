package io.ktor.answers.db

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

typealias LongID = EntityID<Long>
typealias IntID = EntityID<Int>

object UserTable : LongIdTable() {
    val name = varchar("name", 50)
    val passwordHash = varchar("password_hash", 100)
    val active = bool("active")
    val email = text("email")
    val createdAt = datetime("created_at")
    override val tableName = "users"
}


class User(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<User>(UserTable)

    val name by UserTable.name
    val passwordHash by UserTable.passwordHash
    val active by UserTable.active
    val email by UserTable.email
    val createdAt by UserTable.createdAt
    val roles by Role via UserRole
}

object RoleTable : IntIdTable() {
    val name = varchar("name", 100)
    override val tableName = "role"
}

class Role(id: IntID) : IntEntity(id) {
    companion object : IntEntityClass<Role>(RoleTable)

    val name by RoleTable.name
    val users by User via UserRole
}

object UserRole : Table() {
    val user = reference("user", UserTable)
    val role = reference("role", RoleTable)
    override val primaryKey = PrimaryKey(user, role)
}

object ContentUnit : LongIdTable() {
    val text = text("text")
    val author = reference("author_id", UserTable, onDelete = ReferenceOption.CASCADE)
    override val tableName = "content"
}

class Content(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Content>(ContentUnit)

    val text by ContentUnit.text
    val author by ContentUnit.author

}

object VoteTable : LongIdTable() {
    val votee = reference("votee", UserTable, onDelete = ReferenceOption.CASCADE)
    val content = reference("content", ContentUnit)
    val value = bool("value")
    override val tableName = "votes"
}

class Vote(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VoteTable)

    val votee by VoteTable.votee
    val content by VoteTable.content
    val value by VoteTable.value
}

object QuestionTable : LongIdTable() {
    val data = reference("content", ContentUnit)
    override val tableName = "question"
}

class Question(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VoteTable)

    val data by QuestionTable.data
}

object AnswerTable : LongIdTable() {
    val question = reference("question", QuestionTable)
    val data = reference("data", ContentUnit)
    override val tableName = "answer"
}

class Answer(id:LongID):LongIdTable(){
    val 
}


fun main() {
}
