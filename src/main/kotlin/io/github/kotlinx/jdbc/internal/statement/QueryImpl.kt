package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.spi.ColumnMapper
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.jdbc.statement.Query
import java.sql.ResultSet

internal class QueryImpl internal constructor(handle: Handle, sql: String): AbstractSqlStatement<Query>(handle, sql), Query {

    private val resultProducer = object: ResultSetProducer {
        override fun <R> withResultSet(block: (ResultSet) -> R): R {
            return executeInternal { it.executeQuery().use { rs -> block(rs) } }
        }
    }

    override fun <T> map(mapper: RowMapper<T>): ResultIterable<T> {
        return ResultIterableImpl(mapper, resultProducer)
    }

    override fun <T> map(columnIndex: Int, mapper: ColumnMapper<T>): ResultIterable<T> {
        val rowMapper: RowMapper<T> = { rs -> mapper.map(rs, columnIndex) }
        return ResultIterableImpl( rowMapper, resultProducer)
    }

    override fun <T> map(columnLabel: String, mapper: ColumnMapper<T>): ResultIterable<T> {
        val rowMapper: RowMapper<T> = { rs -> mapper.map(rs, columnLabel) }
        return ResultIterableImpl(rowMapper, resultProducer)
    }
}