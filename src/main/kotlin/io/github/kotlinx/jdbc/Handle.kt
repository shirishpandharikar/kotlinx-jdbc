package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.argument.SqlArgumentRegistry
import io.github.kotlinx.jdbc.internal.statement.BatchImpl
import io.github.kotlinx.jdbc.internal.statement.PrepareBatchImpl
import io.github.kotlinx.jdbc.internal.statement.QueryImpl
import io.github.kotlinx.jdbc.internal.statement.UpdateImpl
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreator
import io.github.kotlinx.jdbc.internal.statement.named.cache.ParsedSqlCache
import io.github.kotlinx.jdbc.statement.Batch
import io.github.kotlinx.jdbc.statement.PreparedBatch
import io.github.kotlinx.jdbc.statement.Query
import io.github.kotlinx.jdbc.statement.Update
import io.github.kotlinx.jdbc.tx.TransactionContext
import io.github.kotlinx.jdbc.tx.TransactionOptions
import java.sql.Connection

class Handle(private val kdbi: Kdbi): AutoCloseable {

    private var _connection: Connection? = null
    private var txContext: TransactionContext? = null

    fun getConnection(): Connection = _connection ?: kdbi.dataSource.connection.also { _connection = it }

    fun query(sql: String): Query = QueryImpl(this, sql)
    fun update(sql: String): Update = UpdateImpl(this, sql)
    fun batch(): Batch = BatchImpl(this)
    fun preparedBatch(sql: String): PreparedBatch = PrepareBatchImpl(this, sql)

    fun <R> inTransaction(block: Handle.() -> R): R = inTransaction(TransactionOptions(), block)

    fun <R> inTransaction(options: TransactionOptions, block: Handle.() -> R): R {
        val conn = getConnection()
        val context = txContext ?: TransactionContext().also { txContext = it }
        context.begin(conn, options)
        try {
            val result = this.block()
            context.commit(conn)
            return result
        } catch (ex: Throwable) {
            val shouldRollback = options.shouldRollback(ex)
            try {
                if (shouldRollback) {
                    context.rollback(conn)
                } else {
                    context.commit(conn)
                }
            } catch (re: Exception) {
                ex.addSuppressed(re)
            }
            throw ex
        } finally {
            context.end(conn)
        }
    }

    fun useTransaction(block: Handle.() -> Unit) = inTransaction { block() }

    fun useTransaction(options: TransactionOptions, block: Handle.() -> Unit) = inTransaction(options) { block() }

    override fun close() {
        _connection?.close()
        _connection = null
    }

    internal fun sqlArgumentRegistry(): SqlArgumentRegistry = kdbi.sqlArgumentRegistry()
    internal fun statementCreator(): StatementCreator = kdbi.statementCreator()
    internal fun parsedSqlCache() : ParsedSqlCache = kdbi.parsedSqlCache()
}