package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.internal.result.ResultIterable
import io.github.kotlinx.jdbc.internal.statement.SqlStatement
import io.github.kotlinx.jdbc.spi.RowMapper

interface PreparedBatch: SqlStatement<PreparedBatch> {
    fun add(): PreparedBatch
    fun execute(): IntArray
    fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T>
}