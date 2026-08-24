package io.github.kotlinx.jdbc.internal.result

import java.sql.ResultSet

/**
 * A small abstraction that hands a live ResultSet to a caller-supplied block and (through executeInternal)
 * guarantees the [java.sql.PreparedStatement] and [ResultSet] are closed afterward
 *
 * It decouples row iteration from statement execution/resource lifecycle
 */
internal interface ResultSetProducer {
    fun <R> withResultSet(block: (ResultSet) -> R): R
}