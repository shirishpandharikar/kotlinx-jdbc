package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.github.kotlinx.jdbc.exception.EmptyResultException
import io.github.kotlinx.jdbc.exception.TooManyRowsException
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.test.User
import io.github.kotlinx.test.UserStatus
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.maps.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

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

        should("one() throws TooManyRowsException when more than one row") {
            shouldThrow<TooManyRowsException> {
                dbi.withHandle {
                    query("SELECT id, name, status, age FROM users").map(userMapper).one()
                }
            }
        }

        should("one() throws EmptyResultException when no rows") {
            shouldThrow<EmptyResultException> {
                dbi.withHandle {
                    query("SELECT id, name, status, age FROM users WHERE id = ?")
                        .bind(1, 9999L).map(userMapper).one()
                }
            }
        }

        should("oneOrNull() throws TooManyRowsException when more than one row") {
            shouldThrow<TooManyRowsException> {
                dbi.withHandle {
                    query("SELECT id, name, status, age FROM users").map(userMapper).one()
                }
            }
        }

        should("oneOrNull() returns null when no rows") {
            val result = dbi.withHandle {
                query("SELECT id, name, status, age FROM users WHERE id = ?")
                    .bind(1, 9999L).map(userMapper).oneOrNull()
            }
            result shouldBe null
        }

        context("positional parameter validation") {

            should("throw when position index is zero") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ?")
                            .bind(0, 1L)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Positional parameter index must be >= 1, got 0"
            }

            should("throw when position index is negative") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ?")
                            .bind(-1, 1L)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Positional parameter index must be >= 1, got -1"
            }

            should("throw when bound count is less than placeholder count") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ? AND age = ?")
                            .bind(1, 1L)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Expected 2 positional parameter(s) but got 1"
            }

            should("throw when bound count is greater than placeholder count") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ?")
                            .bind(1, 1L)
                            .bind(2, 99L)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Expected 1 positional parameter(s) but got 2"
            }

            should("throw when position index is out of range") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ?")
                            .bind(5, 1L)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Missing positional parameter at index 1"
            }

            should("throw when there is a gap in positional indices") {
                shouldThrow<IllegalArgumentException> {
                    dbi.withHandle {
                        query("SELECT id FROM users WHERE id = ? AND age = ?")
                            .bind(1, 1L)
                            .bind(3, 25)
                            .map(userMapper)
                            .list()
                    }
                }.message shouldContain "Missing positional parameter at index 2"
            }
        }
    }
}