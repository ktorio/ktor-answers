package io.ktor.answers.db

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
import kotlin.random.Random

typealias LongID = EntityID<Long>
typealias IntID = EntityID<Int>

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
    val user = reference("user", UserTable, onDelete = CASCADE)
    val role = reference("role", RoleTable, onDelete = CASCADE)
    override val primaryKey = PrimaryKey(user, role)
}


object ContentUnit : LongIdTable("content") {
    val text = text("text")
    val author = reference("author_id", UserTable, onDelete = CASCADE)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }

}

object VoteTable : LongIdTable("vote") {
    val voter = reference("voter", UserTable, onDelete = CASCADE)
    val content = reference("content", ContentUnit, onDelete = CASCADE)
    val value = bool("value")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now().toKotlinLocalDateTime() }
}

object QuestionTable : LongIdTable("question") {
    val data = reference("content", ContentUnit, onDelete = CASCADE)
}

object AnswerTable : LongIdTable("answer") {
    val question = reference("question", QuestionTable, onDelete = CASCADE)
    val data = reference("data", ContentUnit, onDelete = CASCADE)
}

object CommentTable : LongIdTable("comment") {
    val data = reference("data", ContentUnit, onDelete = CASCADE)
    val parent = reference("parent", ContentUnit, onDelete = CASCADE)
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

class Role(id: IntID) : IntEntity(id) {
    companion object : IntEntityClass<Role>(RoleTable)

    var name by RoleTable.name
    var users by User via UserRole
    override fun toString(): String = "Role(name='$name')"
}

class Content(id: LongID) : LongEntity(id) {

    companion object : LongEntityClass<Content>(ContentUnit)

    var text by ContentUnit.text
    var author by User referencedOn ContentUnit.author
    var createdAt by ContentUnit.createdAt


    override fun toString(): String = "Content(text='$text', author=$author, createdAt=$createdAt)"
}

class Vote(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VoteTable)

    var voter by User referencedOn VoteTable.voter
    var value by VoteTable.value
    var createdAt by VoteTable.createdAt
    var content by Content referencedOn VoteTable.content
    override fun toString(): String = "Vote(voter=$voter, content=$content, value=$value, createdAt=$createdAt)"

}


class Question(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Question>(QuestionTable)

    var data by Content referencedOn QuestionTable.data
    override fun toString(): String = "Question(data=$data)"

}


class Answer(id: LongID) : LongEntity(id) {
    companion object : LongEntityClass<Answer>(AnswerTable)

    var data by Content referencedOn AnswerTable.data
    var question by Question referencedOn AnswerTable.question
    override fun toString(): String = "Answer(data=$data, question=$question)"

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
        for (number in 1..100) {
            pasha.createQuestion(number)
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

private fun User.createQuestion(id: Int) {
    val q = Question.new {
        data = Content.new {
            text = "Content $id"
            author = this@createQuestion
        }
    }
    for (it in 1..3) {
        q.addComment(it, this)
    }
    for (it in 1..2) {
        q.addAnswerWithComments(it, this)
    }
    if (Random.nextBoolean()){
        Vote.new {
            value = Random.nextBoolean()
            voter = this@createQuestion
            content = q.data
        }
    }
    println("Votes: ${q.data.countVotes()}")
}

fun Content.countVotes(): Int {
    val voteToInt = Expression.build {
        val caseExpr = case().When(VoteTable.value.eq(true), intLiteral(1)).Else(intLiteral(-1))
        Sum(caseExpr, IntegerColumnType())
    }
    return VoteTable
        .slice(voteToInt)
        .select { VoteTable.content eq this@countVotes.id }
        .map { it[voteToInt] }
        .firstOrNull() ?: 0

}

private fun Question.addAnswerWithComments(number: Int, pasha: User) {
    val answer = Answer.new {
        data = Content.new {
            text = "Answer #$number to Question ${this@addAnswerWithComments.id}"
            author = pasha
        }
        this.question = this@addAnswerWithComments
    }
    for (it in 1..3) {
        answer.addComment(it, pasha)
    }
}

private fun Answer.addComment(number: Int, pasha: User) {
    Comment.new {
        data = Content.new {
            text = "Comment #$number to Answer ${this@addComment.id}"
            author = pasha
        }
        parent = this@addComment.data
    }
}

private fun Question.addComment(number: Int, pasha: User) {
    Comment.new {
        data = Content.new {
            text = "Comment #$number to Question ${this@addComment.id}"
            author = pasha
        }
        parent = this@addComment.data
    }
}
