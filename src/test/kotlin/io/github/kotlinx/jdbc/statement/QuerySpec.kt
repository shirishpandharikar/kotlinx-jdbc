package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.test.User
import io.github.kotlinx.test.UserStatus
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe

class QuerySpec : BaseSpec() {

    private val userMapper = RowMapper {
        User(
            id = it.getLong("id"),
            name = it.getString("name"),
            status = UserStatus.valueOf(it.getString("status")),
            age = it.getInt("age")
        )
    }

    init {

        should("find a user by id") {
            val user = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = ?")
                    .bind(1, 1L)
                    .map(userMapper)
                    .first()
            }
            assertSoftly(user) {
                id shouldBe 1L
                name shouldBe "Alice"
                status shouldBe UserStatus.ACTIVE
                age shouldBe 30
            }
        }

        should("return null when no user exists with id") {
            val user = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = ?")
                    .bind(1, 9999L)
                    .map(userMapper)
                    .firstOrNull()
            }
            user shouldBe null
        }

        should("find all users") {
            val users = dbi.withHandle {
                query("SELECT id, name, status, age FROM users")
                    .map(userMapper)
                    .list()
            }

            assertSoftly(users) {
                shouldNotBeEmpty()
                size shouldBeGreaterThanOrEqual 3
            }
        }

        should("find all active users") {

            val users = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE status = ?")
                    .bind(1, "ACTIVE")
                    .map(userMapper)
                    .list()
            }

            assertSoftly(users) {
                shouldNotBeEmpty()
                size shouldBeGreaterThanOrEqual 2
            }
        }
    }

}