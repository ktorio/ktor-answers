package io.ktor.answers.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random


fun main() {
    Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
    transaction {
//        addLogger(StdOutSqlLogger)
        SchemaUtils.create(
            UserTable,
            RoleTable,
            UserRole,
            ContentTable,
            VoteTable,
            QuestionTable,
            AnswerTable,
            CommentTable,
            TagTable,
            QuestionTags
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
            QuestionTable.innerJoin(ContentTable)
                .slice(QuestionTable.columns)
                .select {
                    ContentTable.text like LikePattern("%96")
                }
        ).forEach(::println)

    }
}

val allowedVotes = arrayOf(1.toShort(), -1)
private fun User.createQuestion(id: Int) {
    val q = Question.new {
        data = Content.new {
            text = "Content $id"
            author = this@createQuestion
        }
        title = "Question #$id"
        tags = SizedCollection(Tag.new { name = "tag-1-q-$id" }, Tag.new { name = "tag-2-q-$id" })
    }
    for (it in 1..3) {
        q.addComment(it, this)
    }
    for (it in 1..2) {
        q.addAnswerWithComments(it, this)
    }
    if (Random.nextBoolean()) {
        Vote.new {
            value = allowedVotes.random()
            voter = this@createQuestion
            content = q.data
        }
    }
    println("Votes: ${q.data.countVotes()}")
}

fun Content.countVotes(): Short {
    val sumVotes = Expression.build {
        Sum(VoteTable.value, ShortColumnType())
    }
    return VoteTable
        .slice(sumVotes)
        .select { VoteTable.content eq this@countVotes.id }
        .map { it[sumVotes] }
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
