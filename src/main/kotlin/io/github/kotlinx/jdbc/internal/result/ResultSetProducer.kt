package io.github.kotlinx.jdbc.internal.result

import java.sql.ResultSet

internal interface ResultSetProducer {
    fun <R> withResultSet(block: (ResultSet) -> R): R
}