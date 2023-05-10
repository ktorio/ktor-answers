package io.ktor.answers

import io.ktor.answers.db.*
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
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
            User.new {
                name = Random.nextString(7)
                email = Random.email()
                passwordHash = "***secret***"
                active = false
                displayName = Random.nextString(7)
            }
        }
        assertEquals(current, userRepository.allUsers().size)
        newSuspendedTransaction { UserTable.deleteAll() }
    }

    @Test
    fun `insertion of an active user increases number of users in DB by 1`()  = runTest {
        val current = userRepository.allUsers().size
        newSuspendedTransaction {
            User.new {
                name = Random.nextString(7)
                email = Random.email()
                passwordHash = "***secret***"
                active = true
                displayName = Random.nextString(7)
            }
        }
        assertEquals(current + 1, userRepository.allUsers().size)
        newSuspendedTransaction { UserTable.deleteAll() }
    }
}

val charPool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()
fun Random.Default.nextString(length: Int) = (1..length)
    .map { charPool.random() }
    .joinToString("")

fun Random.Default.email() = nextString(7) + '@' + nextString(5) + '.' + nextString(3)