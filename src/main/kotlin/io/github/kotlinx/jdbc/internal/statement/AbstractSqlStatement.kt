package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.sql.PreparedStatement

internal abstract class AbstractSqlStatement<S : SqlStatement<S>>(handle: Handle, sql: String) : AbstractBaseSqlStatement<S>(handle, sql) {

    @Suppress("UNCHECKED_CAST")
    override fun bind(position: Int, value: Any): S {
        bindings.addPositional(position, value)
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(position: Int, value: SqlArgument): S {
        bindings.addPositional(position, value)
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(name: String, value: Any): S {
        bindings.addNamed(name, value)
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(name: String, value: SqlArgument): S {
        bindings.addNamed(name, value)
        return this as S
    }

    protected fun <R> executeInternal(action: (PreparedStatement) -> R): R {
        val pstmt = createStatement(parsedSql.jdbcSql)
        return pstmt.use {
            applyQueryParameters(it)
            action(it)
        }
    }

    protected open fun createStatement(parsedSql: String): PreparedStatement {
        return statementCreator.createPreparedStatement(connection, parsedSql, context)
    }

    protected open fun applyQueryParameters(preparedStatement: PreparedStatement) {
        resolveParameters().forEach { (i, sqlArgument) ->
            sqlArgument.apply(i, preparedStatement)
        }
    }
}