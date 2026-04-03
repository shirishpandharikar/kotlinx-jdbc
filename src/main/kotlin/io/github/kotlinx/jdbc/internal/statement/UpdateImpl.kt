package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.jdbc.statement.Update
import java.sql.ResultSet

internal class UpdateImpl internal constructor (handle: Handle, sql: String): AbstractSqlStatement<Update>(handle, sql), Update {

    private val resultProducer = object: ResultSetProducer {
        override fun <R> withResultSet(block: (ResultSet) -> R): R {
            return executeInternal {
                it.executeUpdate()
                it.generatedKeys.use { rs -> block(rs) }
            }
        }
    }

    override fun execute() = executeInternal { it.executeUpdate() }

    override fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T> {
        getContext().useGeneratedKeys(*generateKeyColumns)
        return ResultIterableImpl(mapper, resultProducer)
    }
}