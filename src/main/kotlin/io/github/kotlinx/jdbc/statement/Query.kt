package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.internal.statement.ResultIterable
import io.github.kotlinx.jdbc.internal.statement.SqlStatement
import io.github.kotlinx.jdbc.spi.ColumnMapper
import io.github.kotlinx.jdbc.spi.RowMapper

interface Query: SqlStatement<Query> {
    fun <T> map(mapper: RowMapper<T>): ResultIterable<T>
    fun <T> map(columnIndex: Int, mapper: ColumnMapper<T>): ResultIterable<T>
    fun <T> map(columnLabel: String, mapper: ColumnMapper<T>): ResultIterable<T>
}