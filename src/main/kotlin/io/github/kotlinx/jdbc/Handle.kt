package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.argument.SqlArgumentRegistry
import io.github.kotlinx.jdbc.internal.statement.BatchImpl
import io.github.kotlinx.jdbc.internal.statement.PrepareBatchImpl
import io.github.kotlinx.jdbc.internal.statement.QueryImpl
import io.github.kotlinx.jdbc.internal.statement.UpdateImpl
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreator
import io.github.kotlinx.jdbc.statement.Batch
import io.github.kotlinx.jdbc.statement.PreparedBatch
import io.github.kotlinx.jdbc.statement.Query
import io.github.kotlinx.jdbc.statement.Update
import java.sql.Connection

class Handle(private val kdbi: Kdbi): AutoCloseable {

    private var _connection: Connection? = null

    fun getConnection(): Connection = _connection ?: kdbi.dataSource.connection.also { _connection = it }

    fun query(sql: String): Query = QueryImpl(this, sql)
    fun update(sql: String): Update = UpdateImpl(this, sql)
    fun batch(): Batch = BatchImpl(this)
    fun preparedBatch(sql: String): PreparedBatch = PrepareBatchImpl(this, sql)

    fun <R> withTransaction(block: Handle.() -> R): R = TODO("Not implemented yet")
    fun useTransaction(block: Handle.() -> Unit) = withTransaction { block() }

    override fun close() {
        _connection?.close()
        _connection = null
    }

    internal fun sqlArgumentRegistry(): SqlArgumentRegistry = kdbi.sqlArgumentRegistry()
    internal fun statementCreator(): StatementCreator = kdbi.statementCreator()
}