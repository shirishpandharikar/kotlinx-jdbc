package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.result.ResultIterableImpl
import io.github.kotlinx.jdbc.result.ResultIterable
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.jdbc.statement.Update

internal class UpdateImpl internal constructor (handle: Handle, sql: String): AbstractSqlStatement<Update>(handle, sql), Update {

    private val resultProducer = resultSetProducer {
        it.executeUpdate()
        it.generatedKeys
    }

    override fun execute() = executeInternal { it.executeUpdate() }

    override fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T> {
        context.useGeneratedKeys(*generateKeyColumns)
        return ResultIterableImpl(mapper, resultProducer)
    }
}