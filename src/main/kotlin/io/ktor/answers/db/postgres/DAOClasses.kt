package io.ktor.answers.db.postgres

//import io.ktor.answers.model.*
//import io.ktor.answers.model.old.*
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

typealias LongId = EntityID<Long>
typealias IntId = EntityID<Int>

class UserDAO(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<UserDAO>(UserTable)

    var name by UserTable.name
    var passwordHash by UserTable.passwordHash
    var active by UserTable.active

    var email by UserTable.email
    var createdAt by UserTable.createdAt
    var roles by Role via UserRole
    var displayName by UserTable.displayName
    var location by UserTable.location
    var aboutMe by UserTable.aboutMe
    var link by UserTable.link
    val contentItems by Content referrersOn ContentTable.author
    override fun toString(): String =
        "User(name='$name', passwordHash='$passwordHash', active=$active, email='$email', createdAt=$createdAt)"
}

//fun UserDAO.toDTO() = UserDTO(
//    id.value, name, active, email, createdAt, displayName, location, aboutMe, link
//)

class Tag(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<Tag>(TagTable)

    var name by TagTable.name
    var questions by QuestionDAO via QuestionTags
    override fun toString(): String = "Tag(name='$name')"
}

class Role(id: IntId) : IntEntity(id) {
    companion object : IntEntityClass<Role>(RoleTable)

    var name by RoleTable.name
    var users by UserDAO via UserRole
    override fun toString(): String = "Role(name='$name')"
}

class Content(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<Content>(ContentTable)

    var text by ContentTable.text
    var author by UserDAO referencedOn ContentTable.author
    var createdAt by ContentTable.createdAt

    override fun toString(): String = "Content(text='$text', author=$author, createdAt=$createdAt)"
}

class Vote(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<Vote>(VoteTable)

    var voter by UserDAO referencedOn VoteTable.voter
    var value by VoteTable.value
    var createdAt by VoteTable.createdAt
    var content by Content referencedOn VoteTable.content
    override fun toString(): String = "Vote(voter=$voter, content=$content, value=$value, createdAt=$createdAt)"
}


class QuestionDAO(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<QuestionDAO>(QuestionTable)

    var data by Content referencedOn QuestionTable.data
    // var votes by Vote referencedOn VoteTable.value
    var tags by Tag via QuestionTags
    var title by QuestionTable.title
    val answers by AnswerDAO referrersOn AnswerTable.question
    override fun toString(): String = "Question(data=$data, title='$title')"
}

//fun QuestionDAO.toDTO() = QuestionDTO(
//    id = id.value,
//    title = title,
//    text = data.text,
//    createdAt = data.createdAt,
//    authorId = data.author.id.value,
//    votes = 0// TODO: votes.value.toInt()
//)

class AnswerDAO(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<AnswerDAO>(AnswerTable)

    var data by Content referencedOn AnswerTable.data
    var question by QuestionDAO referencedOn AnswerTable.question
    var accepted by AnswerTable.accepted
    override fun toString(): String = "Answer(data=$data, question=$question)"
}


class CommentDAO(id: LongId) : LongEntity(id) {
    companion object : LongEntityClass<CommentDAO>(CommentTable)

    var data by Content referencedOn CommentTable.data
    var parent by Content referencedOn CommentTable.parent
    override fun toString(): String = "Comment(data=$data, parent=$parent)"
}
