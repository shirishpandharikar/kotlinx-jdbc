package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.statement.QueryImpl
import io.github.kotlinx.jdbc.internal.statement.UpdateImpl
import io.github.kotlinx.jdbc.statement.Query
import io.github.kotlinx.jdbc.statement.Update
import java.sql.Connection
import javax.sql.DataSource

class Handle(private val dataSource: DataSource): AutoCloseable {

    private var _connection: Connection? = null

    fun getConnection(): Connection =
        _connection ?: dataSource.connection.also { _connection = it }

    fun query(sql: String): Query = QueryImpl(this, sql)
    fun update(sql: String): Update = UpdateImpl(this, sql)

    fun <R> withTransaction(block: Handle.() -> R): R = TODO("Not implemented yet")
    fun useTransaction(block: Handle.() -> Unit) = withTransaction { block() }

    override fun close() {
        _connection?.close()
        _connection = null
    }
}