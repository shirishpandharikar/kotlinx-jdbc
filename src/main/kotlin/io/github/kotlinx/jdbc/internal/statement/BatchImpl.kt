package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext
import io.github.kotlinx.jdbc.statement.Batch

class BatchImpl(private val handle: Handle): Batch {

    private val sqls = mutableListOf<String>()

    override fun add(sql: String): Batch {
        require(sql.isNotBlank()) { "Batch SQL is required" }
        sqls += sql
        return this
    }

    override fun execute(): IntArray {
        //this is not what the user was probably intending
        if (sqls.isEmpty()) return intArrayOf()
        return handle.statementCreator().createStatement(handle.getConnection(), StatementContext()).use { statement ->
            sqls.forEach { statement.addBatch(it) }
            statement.executeBatch()
        }
    }
}