package io.ktor.answers

import io.ktor.answers.db.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals


@Testcontainers
class DbTest : AbstractDbTest() {

    @Test
    fun `deactivated users should not be in the 'all users' response`() {
        val current = UserRepository.allUsers().size
        transaction {
            User.new {
                name = Random.nextString(7)
                email = Random.email()
                passwordHash = "***secret***"
                active = false
                displayName = Random.nextString(7)
            }
        }
        assertEquals(current, UserRepository.allUsers().size)
    }

    @Test
    fun `insertion of an active user increases number of users in DB by 1`() {
        val current = UserRepository.allUsers().size
        transaction {
            User.new {
                name = Random.nextString(7)
                email = Random.email()
                passwordHash = "***secret***"
                active = true
                displayName = Random.nextString(7)
            }
        }
        assertEquals(current + 1, UserRepository.allUsers().size)
    }
}

val charPool = (('a'..'z') + ('A'..'Z') + ('0'..'9')).toCharArray()
fun Random.Default.nextString(length: Int) = (1..length)
    .map { charPool.random() }
    .joinToString("")

fun Random.Default.email() = nextString(7) + '@' + nextString(5) + '.' + nextString(3)