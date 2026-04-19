package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.kotest.matchers.shouldBe

class PreparedBatchSpec : BaseSpec() {

    init {
        should("execute prepared batch insert") {
            val rowsInserted = dbi.withHandle {
                preparedBatch("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                    .bind(1, "Dave")
                    .bind(2, "ACTIVE")
                    .bind(3, 40)
                    .add()
                    .bind(1, "Eve")
                    .bind(2, "INACTIVE")
                    .bind(3, 25)
                    .add()
                    .execute()
            }
            rowsInserted.size shouldBe 2
        }

        should("execute prepared batch insert and return generated keys") {
            val generatedIds = dbi.withHandle {
                preparedBatch("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                    .bind(1, "Frank")
                    .bind(2, "ACTIVE")
                    .bind(3, 28)
                    .add()
                    .bind(1, "Grace")
                    .bind(2, "INACTIVE")
                    .bind(3, 32)
                    .add()
                    .executeWithGeneratedKeys("id") { rs ->
                        rs.getInt("id")
                    }.list()
            }
            generatedIds.size shouldBe 2
        }

    }

}