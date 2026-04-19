package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.result.ResultIterable
import io.github.kotlinx.jdbc.internal.result.ResultIterableImpl
import io.github.kotlinx.jdbc.internal.result.ResultSetProducer
import io.github.kotlinx.jdbc.spi.RowMapper
import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.statement.PreparedBatch
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.TreeMap

internal class PrepareBatchImpl internal constructor(handle: Handle, sql: String): AbstractSqlStatement<PreparedBatch>(handle, sql), PreparedBatch {

    private val batches = mutableListOf<BatchRow>()

    private data class BatchRow(val positional: TreeMap<Int, SqlArgument>)

    private val resultProducer = object: ResultSetProducer {
        override fun <R> withResultSet(block: (ResultSet) -> R): R {
            return executeInternal {
                it.executeBatch()
                it.generatedKeys.use { rs -> block(rs) }
            }
        }
    }

    override fun add(): PreparedBatch {
        batches.add(BatchRow(TreeMap(positionalParams)))
        positionalParams.clear()
        return this
    }

    override fun execute(): IntArray {
        return executeInternal { it.executeBatch() }
    }

    override fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T> {
        context.useGeneratedKeys(*generateKeyColumns)
        return ResultIterableImpl(mapper, resultProducer)
    }

    override fun applyQueryParameters(preparedStatement: PreparedStatement) {
        batches.forEach { batch ->
            batch.positional.forEach { (i, binding) ->
                binding.apply(i, preparedStatement)
            }
            preparedStatement.addBatch()
        }
    }
}