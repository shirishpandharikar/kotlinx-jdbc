package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.kotest.matchers.collections.shouldHaveSize

class BatchSpec: BaseSpec() {

    init {

        should("delete users in batch") {
            val rows = dbi.withHandle {
                batch()
                    .add("DELETE FROM users WHERE id = 1")
                    .add("DELETE FROM users WHERE id = 2")
                    .execute()
            }
            rows shouldHaveSize 2
        }

    }

}