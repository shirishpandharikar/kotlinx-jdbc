package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.BaseSpec
import io.github.kotlinx.jdbc.tx.TransactionOptions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe

class TransactionSpec : BaseSpec() {

    private class BusinessException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

    init {

        should("commit transaction") {
            dbi.useTransaction {
                update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                    .bind(1, "CommitUser")
                    .bind(2, "ACTIVE")
                    .bind(3, 20)
                    .execute()
            }

            dbi.useHandle {
                val count = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "CommitUser")
                    .map { it.getInt(1) }
                    .first()
                count shouldBe 1
                //revert
                update("DELETE FROM users WHERE name = ?")
                    .bind(1, "CommitUser")
                    .execute()
            }
        }

        should("rollback on exception") {
            dbi.withHandle {
                shouldThrow<RuntimeException> {
                    useTransaction(TransactionOptions()) {
                        update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                            .bind(1, "RollbackUser").bind(2, "ACTIVE").bind(3, 21).execute()
                        throw RuntimeException("boom")
                    }
                }
                val count = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "RollbackUser")
                    .map { it.getInt(1) }.first()
                count shouldBe 0
            }
        }

        should("return block result on commit") {
            val result = dbi.withHandle {
                inTransaction(TransactionOptions()) {
                    update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                        .bind(1, "ResultUser")
                        .bind(2, "ACTIVE")
                        .bind(3, 23)
                        .execute()
                }
            }
            result shouldBe 1
            //cleanup
            dbi.withHandle {
                update("DELETE FROM users WHERE name = ?")
                    .bind(1, "ResultUser")
                    .execute()
            }
        }

        should("nested commit - both inner and outer persist") {
            dbi.withHandle {
                useTransaction(TransactionOptions()) {
                    update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                        .bind(1, "Outer1")
                        .bind(2, "ACTIVE")
                        .bind(3, 30)
                        .execute()
                    //this is a nested transaction
                    useTransaction {
                        update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                            .bind(1, "Inner1")
                            .bind(2, "ACTIVE")
                            .bind(3, 31)
                            .execute()
                    }
                }
                //then
                val count = query("SELECT COUNT(*) FROM users WHERE name IN (?, ?)")
                    .bind(1, "Outer1")
                    .bind(2, "Inner1")
                    .map { it.getInt(1) }
                    .first()
                count shouldBe 2
                //cleanup
                update("DELETE FROM users WHERE name IN ('Outer1','Inner1')").execute()
            }
        }

        should("nested inner failure rolls back only inner") {
            dbi.useHandle {
                useTransaction(TransactionOptions()) {
                    update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                        .bind(1, "Outer2")
                        .bind(2, "ACTIVE")
                        .bind(3, 32)
                        .execute()

                    //nested transaction
                    runCatching {
                        useTransaction(TransactionOptions()) {
                            update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                                .bind(1, "Inner2")
                                .bind(2, "ACTIVE")
                                .bind(3, 33)
                                .execute()
                            //simulate an exception
                            throw RuntimeException("inner fails")
                        }
                    }
                }
                //then
                val countOut = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "Outer2")
                    .map { it.getInt(1) }
                    .first()
                countOut shouldBe 1
                val countIn = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "Inner2")
                    .map { it.getInt(1) }
                    .first()
                countIn shouldBe 0
                //cleanup
                update("DELETE FROM users WHERE name = ?")
                    .bind(1, "Outer2")
                    .execute()
            }
        }

        should("outer failure rolls back everything including committed inner") {
            dbi.useHandle {
                shouldThrow<RuntimeException> {
                    useTransaction(TransactionOptions()) {
                        useTransaction {
                            update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                                .bind(1, "Inner3")
                                .bind(2, "ACTIVE")
                                .bind(3, 34)
                                .execute()
                        }
                        throw RuntimeException("outer fails")
                    }
                }
                //then
                val count = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "Inner3")
                    .map { it.getInt(1) }
                    .first()
                count shouldBe 0
            }
        }

        should("commit transaction if block throws exception specified in noRollbackFor") {
            val txOptions = TransactionOptions(noRollbackFor = setOf(BusinessException::class.java))
            dbi.useHandle {
                shouldThrow<BusinessException> {
                    useTransaction(txOptions) {
                        update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                            .bind(1, "CommitUser")
                            .bind(2, "ACTIVE")
                            .bind(3, 20)
                            .execute()
                        throw BusinessException("This is a business specific exception and should not rollback transaction")
                    }
                }
                val count = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "CommitUser")
                    .map { it.getInt(1) }
                    .first()
                count shouldBe 1
                //revert
                update("DELETE FROM users WHERE name = ?")
                    .bind(1, "CommitUser")
                    .execute()
            }
        }

        should("rollback transaction if block throws exception specified in rollbackFor") {
            val txOptions = TransactionOptions(rollbackFor = setOf(UnsupportedOperationException::class.java))
            dbi.useHandle {
                shouldThrow<UnsupportedOperationException> {
                    useTransaction(txOptions) {
                        update("INSERT INTO users (name, status, age) VALUES (?, ?, ?)")
                            .bind(1, "CommitUser")
                            .bind(2, "ACTIVE")
                            .bind(3, 20)
                            .execute()
                        throw UnsupportedOperationException()
                    }
                }
                val count = query("SELECT COUNT(*) FROM users WHERE name = ?")
                    .bind(1, "CommitUser")
                    .map { it.getInt(1) }
                    .first()
                count shouldBe 0
            }
        }
    }

}