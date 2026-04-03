package io.github.kotlinx.jdbc.internal.statement

import java.sql.ResultSet

interface ResultSetProducer {
    fun <R> withResultSet(block: (ResultSet) -> R): R
}