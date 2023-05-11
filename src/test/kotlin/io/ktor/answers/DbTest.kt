package io.ktor.answers

import io.ktor.answers.db.*
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals


@Testcontainers
class DbTest : AbstractDbTest() {
    private val userRepository = UserRepository()

    @AfterTest
    fun cleanup() = runTest { suspendTransaction { UserTable.deleteAll() } }

    @Test
    fun `deactivated users should not be in the 'all users' response`() = runTest {
        val current = userRepository.allUsers().size
        newSuspendedTransaction {
            createRandomUser(active = false)
        }
        assertEquals(current, userRepository.allUsers().size)
    }

    @Test
    fun `insertion of an active user increases number of users in DB by 1`() = runTest {
        val current = userRepository.allUsers().size
        newSuspendedTransaction {
            createRandomUser()
        }
        assertEquals(current + 1, userRepository.allUsers().size)
    }

    @Test
    fun `comments sorting`() = runTest {
        suspendTransaction {
            val voters = (1..3).map {
                createRandomUser()
            }
            val author = createRandomUser()
            val question = Question.new {
                data = Content.new {
                    text = Random.nextString(50)
                    this.author = author
                    title = "question"
                }
            }
            val comment1 = Comment.new {
                data = Content.new {
                    text = "comment1"
                    this.author = voters[0]
                }
                parent = question.data
            }
            val comment2 = Comment.new {
                data = Content.new {
                    text = "comment2"
                    this.author = voters[1]
                }
                parent = question.data
            }
            voters.map {
                Vote.new {
                    voter = it
                    content = comment1.data
                    value = 1
                }
            }
            voters.drop(1).map {
                Vote.new {
                    voter = it
                    content = comment2.data
                    value = 1
                }
            }
        }
        val ids = suspendTransaction {
            UserTable.slice(UserTable.id).selectAll().map { it[UserTable.id].value }
        }
        val sortedByVotes = userRepository.commentsByIds(ids, sortBy = "votes")
        assertEquals("comment2", sortedByVotes[0].text)
        assertEquals("comment1", sortedByVotes[1].text)
        assertEquals(2, sortedByVotes[0].votes)
        assertEquals(3, sortedByVotes[1].votes)
        val sortedByCreation = userRepository.commentsByIds(ids, sortBy = "creation")
        assertEquals("comment1", sortedByCreation[0].text)
        assertEquals("comment2", sortedByCreation[1].text)
    }

    @Test
    fun `question sorting`() = runTest {
        suspendTransaction {
            val author = createRandomUser()
            val question2 = Question.new {
                data = Content.new {
                    text = Random.nextString(50)
                    this.author = author
                    title = "2question"
                }
            }
            val question1 = Question.new {
                data = Content.new {
                    text = Random.nextString(50)
                    this.author = author
                    title = "1question"
                }
            }
            val question3 = Question.new {
                data = Content.new {
                    text = Random.nextString(50)
                    this.author = author
                    title = "3question"
                }
            }
            // chrono order: 2 → 1 → 3
            // title order: 1 → 2 → 3
            // vote order: 3 → 2 → 1
            val voters = (1..3).map { createRandomUser() }
            voters.forEach { it.upvote(question1.data) }
            voters[0].upvote(question2.data)
            voters[0].downvote(question3.data)
        }
        val ids = suspendTransaction {
            UserTable.slice(UserTable.id).selectAll().map { it[UserTable.id].value }
        }
        val sortedByVotes = userRepository.questionsByIds(ids, sortBy = "votes")
        assertEquals("3question", sortedByVotes[0].title)
        assertEquals("2question", sortedByVotes[1].title)
        assertEquals("1question", sortedByVotes[2].title)
        assertEquals(-1, sortedByVotes[0].votes)
        assertEquals(1, sortedByVotes[1].votes)
        assertEquals(3, sortedByVotes[2].votes)
        val sortedByCreation = userRepository.questionsByIds(ids, sortBy = "creation")
        assertEquals("2question", sortedByCreation[0].title)
        assertEquals("1question", sortedByCreation[1].title)
        assertEquals("3question", sortedByCreation[2].title)
        val sortedByTitle = userRepository.questionsByIds(ids, sortBy = "title")
        assertEquals("1question", sortedByTitle[0].title)
        assertEquals("2question", sortedByTitle[1].title)
        assertEquals("3question", sortedByTitle[2].title)
    }

    @Test
    fun `answer sorting`() = runTest {
        suspendTransaction {
            val author = createRandomUser()
            val voters = (1..3).map { createRandomUser() }
            val question = Question.new {
                title = "question"
                data = Content.new {
                    this.author = author
                    text = "question"
                }
            }
            val answer2 = Answer.new {
                this.question = question
                data = Content.new {
                    this.author = voters[0]
                    text = "answer2"
                }
                accepted = false
            }
            val answer1 = Answer.new {
                this.question = question
                data = Content.new {
                    this.author = voters[0]
                    text = "answer1"
                }
                accepted = false
            }
            val answer3 = Answer.new {
                this.question = question
                data = Content.new {
                    this.author = voters[0]
                    text = "answer3"
                }
                accepted = false
            }
            // chrono order: 2 → 1 → 3
            // vote order: 3 → 2 → 1
            voters.forEach { it.upvote(answer1.data) }
            voters[0].upvote(answer2.data)
            voters[0].downvote(answer3.data)
        }
        val ids = suspendTransaction {
            UserTable.slice(UserTable.id).selectAll().map { it[UserTable.id].value }
        }
        val sortedByVotes = userRepository.answersByIds(ids, sortBy = "votes")
        assertEquals("answer3", sortedByVotes[0].text)
        assertEquals("answer2", sortedByVotes[1].text)
        assertEquals("answer1", sortedByVotes[2].text)
        assertEquals(-1, sortedByVotes[0].votes)
        assertEquals(1, sortedByVotes[1].votes)
        assertEquals(3, sortedByVotes[2].votes)
        val sortedByCreation = userRepository.answersByIds(ids, sortBy = "creation")
        assertEquals("answer2", sortedByCreation[0].text)
        assertEquals("answer1", sortedByCreation[1].text)
        assertEquals("answer3", sortedByCreation[2].text)
    }
}

val charPool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()
fun Random.Default.nextString(length: Int) = (1..length)
    .map { charPool.random() }
    .joinToString("")

fun Random.Default.email() = nextString(7) + '@' + nextString(5) + '.' + nextString(3)

private fun createRandomUser(active: Boolean = true) = User.new {
    name = Random.nextString(7)
    email = Random.email()
    passwordHash = "***secret***"
    this.active = active
    displayName = Random.nextString(7)
}

private fun User.upvote(content: Content) {
    Vote.new {
        voter = this@upvote
        value = 1
        this.content = content
    }
}

private fun User.downvote(content: Content) {
    Vote.new {
        voter = this@downvote
        value = -1
        this.content = content
    }
}