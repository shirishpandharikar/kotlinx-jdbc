package io.github.kotlinx.jdbc.core

import io.github.kotlinx.jdbc.dsl.Query
import io.github.kotlinx.jdbc.dsl.Update
import java.sql.Connection
import javax.sql.DataSource

class Handle(private val dataSource: DataSource): AutoCloseable {

    private var _connection: Connection? = null

    fun getConnection(): Connection =
        _connection ?: dataSource.connection.also { _connection = it }

    fun query(sql: String) = Query(this, sql)
    fun update(sql: String) = Update(this, sql)

    override fun close() {
        _connection?.close()
        _connection = null
    }
}