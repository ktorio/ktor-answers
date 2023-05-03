package io.ktor.answers.db

import io.ktor.answers.db.UserTable.clientDefault
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.ReferenceOption.CASCADE
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

typealias LongID = EntityID<Long>
typealias IntID = EntityID<Int>

object UserTable : LongIdTable("users") {
    val name = varchar("name", 50)
    val passwordHash = varchar("password_hash", 100)
    val active = bool("active")
    val email = text("email")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }
}

class User(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<User>(UserTable)

    var name by UserTable.name
    var passwordHash by UserTable.passwordHash
    var active by UserTable.active
    var email by UserTable.email
    var createdAt by UserTable.createdAt
    var roles by Role via UserRole
    override fun toString(): String =
        "User(name='$name', passwordHash='$passwordHash', active=$active, email='$email', createdAt=$createdAt)"

}

object RoleTable : IntIdTable("role") {
    val name = varchar("name", 100).uniqueIndex()
}

class Role(id: IntID) : IntEntity(id) {
    companion object : IntEntityClass<Role>(RoleTable)

    var name by RoleTable.name
    var users by User via UserRole
    override fun toString(): String = "Role(name='$name')"

}

object UserRole : Table("user_role") {
    val user = reference("user", UserTable, onDelete = CASCADE)
    val role = reference("role", RoleTable, onDelete = CASCADE)
    override val primaryKey = PrimaryKey(user, role)
}

object ContentUnit : LongIdTable("content") {
    val text = text("text")
    val author = reference("author_id", UserTable, onDelete = CASCADE)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }

}

class Content(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Content>(ContentUnit)

    var text by ContentUnit.text
    var author by User referencedOn ContentUnit.author
    var createdAt by ContentUnit.createdAt
    override fun toString(): String = "Content(text='$text', author=$author, createdAt=$createdAt)"


}

object VoteTable : LongIdTable("vote") {
    val voter = reference("voter", UserTable, onDelete = CASCADE)
    val content = reference("content", ContentUnit, onDelete = CASCADE)
    val value = bool("value")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }
}

class Vote(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VoteTable)

    var voter by User referencedOn VoteTable.voter
    var content by Content referencedOn VoteTable.content
    var value by VoteTable.value
    var createdAt by VoteTable.createdAt
    override fun toString(): String = "Vote(voter=$voter, content=$content, value=$value, createdAt=$createdAt)"

}

object QuestionTable : LongIdTable("question") {
    val data = reference("content", ContentUnit, onDelete = CASCADE)
}

class Question(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Question>(QuestionTable)

    var data by Content referencedOn QuestionTable.data
    override fun toString(): String = "Question(data=$data)"

}

object AnswerTable : LongIdTable("answer") {
    val question = reference("question", QuestionTable, onDelete = CASCADE)
    val data = reference("data", ContentUnit, onDelete = CASCADE)
}

class Answer(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Answer>(AnswerTable)

    var data by Content referencedOn AnswerTable.data
    var question by Question referencedOn AnswerTable.question
    override fun toString(): String = "Answer(data=$data, question=$question)"

}

object CommentTable : LongIdTable("comment") {
    val data = reference("data", ContentUnit, onDelete = CASCADE)
    val parent = reference("parent", ContentUnit, onDelete = CASCADE)
}

class Comment(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Comment>(CommentTable)

    var data by Content referencedOn CommentTable.data
    var parent by Content referencedOn CommentTable.parent
    override fun toString(): String = "Comment(data=$data, parent=$parent)"

}


fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction {
//        addLogger(StdOutSqlLogger)
        SchemaUtils.create(
            UserTable,
            RoleTable,
            UserRole,
            ContentUnit,
            VoteTable,
            QuestionTable,
            AnswerTable,
            CommentTable
        )

        val pasha = User.new {
            name = "pasha"
            roles = SizedCollection(
                Role.new { name = "ADMIN" },
                Role.new { name = "USER" }
            )
            active = true
            passwordHash = "***NONE***"
            email = "secret@example.com"
        }
        (1..100).forEach {
            Question.new {
                data = Content.new {
                    text = "Content $it"
                    author = pasha
                }
            }
        }

        Question.wrapRows(
            QuestionTable.innerJoin(ContentUnit)
                .slice(QuestionTable.columns)
                .select {
                    ContentUnit.text like LikePattern("%96")
                }
        ).forEach(::println)

    }
}
