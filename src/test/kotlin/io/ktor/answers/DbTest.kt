package io.ktor.answers

import io.ktor.answers.db.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test
import kotlin.test.assertEquals


@Testcontainers
class DbTest : AbstractDbTest() {

    @Test
    fun `deactivated users should not be in the 'all users' response`() {
        val current = UserRepository.allUsers().size
        transaction {
            User.new {
                name = "pasha"
                email = "asm0dey@example.com"
                passwordHash = "***secret***"
                active = false
            }
        }
        assertEquals(current, UserRepository.allUsers().size)
    }

    @Test
    fun `insertion of an active user increases number of users in DB by 1`() {
        val current = UserRepository.allUsers().size
        transaction {
            User.new {
                name = "pasha1"
                email = "asm0dey@example.com1"
                passwordHash = "***secret***"
                active = true
            }
        }
        assertEquals(current + 1, UserRepository.allUsers().size)
    }
}