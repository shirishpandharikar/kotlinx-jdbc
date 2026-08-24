package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.github.kotlinx.test.User
import io.github.kotlinx.test.UserStatus
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateSpec: BaseSpec() {

    init {
        should("update user with id 3 and set status INACTIVE") {
            val rows = dbi.withHandle {
                update("UPDATE users SET status = ? WHERE id = ?")
                    .bind(1, "INACTIVE")
                    .bind(2, 3L)
                    .execute()
            }
            rows shouldBe 1
        }

        should("update user with id 3") {
            val rows = dbi.withHandle {
                update("UPDATE users SET name = ? WHERE id = ?")
                    .bind(1, "Charlie Davis")
                    .bind(2, 3L)
                    .execute()
            }
            rows shouldBe 1
        }

        should("insert new user and return id, name, status and age") {
            val result = dbi.withHandle {
                update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                    .bind(1, "Emily")
                    .bind(2, "ACTIVE")
                    .bind(3, 28)
                    .executeWithGeneratedKeys("id", "name", "status", "age") {
                        User(
                            id = it.getLong("id"),
                            name = it.getString("name"),
                            status = UserStatus.valueOf(it.getString("status")),
                            age = it.getInt("age")
                        )
                    }.first()
            }
            assertSoftly(result) {
                id.shouldNotBeNull()
                name shouldBe "Emily"
                status shouldBe UserStatus.ACTIVE
                age shouldBe 28
            }
        }
    }

}