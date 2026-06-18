package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.test.User
import io.github.kotlinx.test.UserStatus
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.maps.shouldNotBeEmpty
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
                    .bind(1, 3L)
                    .map(userMapper)
                    .first()
            }
            assertSoftly(user) {
                id shouldBe 3L
                name shouldBe "Charlie"
                status shouldBe UserStatus.ACTIVE
                age shouldBe 35
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

        should("associate user by id") {
            val result = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = ?")
                    .bind(1, 3L)
                    .map(userMapper)
                    .associateBy { it.id }
            }
            assertSoftly(result) {
                shouldNotBeEmpty()
                size shouldBe 1
                assertSoftly(getValue(3L)) {
                    name shouldBe "Charlie"
                    status shouldBe UserStatus.ACTIVE
                    age shouldBe 35
                }
            }
        }

        should("associate user by id with name value") {
            val result = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = ?")
                    .bind(1, 3L)
                    .map(userMapper)
                    .associateBy({ it.id }, { it.name })
            }
            assertSoftly(result) {
                shouldNotBeEmpty()
                size shouldBe 1
                getValue(3L) shouldBe "Charlie"
            }
        }

        should("find a user by named parameter id") {
            val user = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = :id")
                    .bind("id", 3L)
                    .map(userMapper)
                    .first()
            }
            assertSoftly(user) {
                id shouldBe 3L
                name shouldBe "Charlie"
                status shouldBe UserStatus.ACTIVE
                age shouldBe 35
            }
        }

        should("find all users with named parameter status 'ACTIVE'") {

            val users = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE status = :status")
                    .bind("status", "ACTIVE")
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