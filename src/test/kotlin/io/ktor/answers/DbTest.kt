package io.ktor.answers

import io.ktor.answers.db.*
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals


@Testcontainers
class DbTest : AbstractDbTest() {
    private val userRepository = UserRepository()

    @Test
    fun `deactivated users should not be in the 'all users' response`() = runTest {
        val current = userRepository.allUsers().size
        newSuspendedTransaction {
            createRandomUser(active = false)
        }
        assertEquals(current, userRepository.allUsers().size)
        newSuspendedTransaction { UserTable.deleteAll() }
    }

    @Test
    fun `insertion of an active user increases number of users in DB by 1`() = runTest {
        val current = userRepository.allUsers().size
        newSuspendedTransaction {
            createRandomUser()
        }
        assertEquals(current + 1, userRepository.allUsers().size)
        newSuspendedTransaction { UserTable.deleteAll() }
    }

    @Test
    fun `test comments sorting`() = runTest {
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
            User.all().map { it.id.value }.toList()
        }
        val sortedByVotes = userRepository.commentsByIds(ids, sortBy = "votes")
        assertEquals("comment2", sortedByVotes[0].text)
        assertEquals("comment1", sortedByVotes[1].text)
        val sortedByCreation = userRepository.commentsByIds(ids, sortBy = "creation")
        assertEquals("comment1", sortedByCreation[0].text)
        assertEquals("comment2", sortedByCreation[1].text)
        suspendTransaction { UserTable.deleteAll() }
    }

    private fun createRandomUser(active: Boolean = true) = User.new {
        name = Random.nextString(7)
        email = Random.email()
        passwordHash = "***secret***"
        this.active = active
        displayName = Random.nextString(7)
    }
}

val charPool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()
fun Random.Default.nextString(length: Int) = (1..length)
    .map { charPool.random() }
    .joinToString("")

fun Random.Default.email() = nextString(7) + '@' + nextString(5) + '.' + nextString(3)