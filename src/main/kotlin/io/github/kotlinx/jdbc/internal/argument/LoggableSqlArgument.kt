package io.github.kotlinx.jdbc.internal.argument

import io.github.kotlinx.jdbc.spi.Loggable
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.sql.PreparedStatement

internal class LoggableSqlArgument<T>(
    private val value: T,
    private val logFormatter: (T) -> String = { it.toString() },
    private val binder: (position: Int, pstmt: PreparedStatement, value: T) -> Unit
) : SqlArgument, Loggable {
    override fun apply(position: Int, pstmt: PreparedStatement) = binder(position, pstmt, value)
    override fun log(): String = logFormatter(value)
    override fun toString(): String = "Argument(value=${log()})"
}