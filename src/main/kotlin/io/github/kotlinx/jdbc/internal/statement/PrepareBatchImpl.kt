package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.result.ResultIterable
import io.github.kotlinx.jdbc.internal.result.ResultIterableImpl
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.statement.PreparedBatch
import java.sql.PreparedStatement

internal class PrepareBatchImpl internal constructor(handle: Handle, sql: String): AbstractSqlStatement<PreparedBatch>(handle, sql), PreparedBatch {

    private val batches = mutableListOf<BatchRow>()

    private data class BatchRow(val params: Map<Int, SqlArgument>)

    private val resultSetProducer = resultSetProducer {
        it.executeBatch()
        it.generatedKeys
    }

    override fun add(): PreparedBatch {
        if (bindings.isEmpty()) {
            throw IllegalStateException(
                "Attempt to add() an empty batch, you probably didn't mean to do this "
                        + "- call add() *after* setting batch parameters"
            )
        }
        val resolvedParams = resolveParameters()
        batches.add(BatchRow(resolvedParams))
        bindings.clear()
        return this
    }

    override fun execute(): IntArray {
        return executeInternal { it.executeBatch() }
    }

    override fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T> {
        context.useGeneratedKeys(*generateKeyColumns)
        return ResultIterableImpl(mapper, resultSetProducer)
    }

    override fun applyQueryParameters(preparedStatement: PreparedStatement) {
        batches.forEach { batch ->
            batch.params.forEach { (i, binding) ->
                binding.apply(i, preparedStatement)
            }
            preparedStatement.addBatch()
        }
    }
}