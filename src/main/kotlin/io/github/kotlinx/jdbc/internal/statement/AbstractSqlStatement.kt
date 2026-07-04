package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.result.ResultSetProducer
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.sql.PreparedStatement
import java.sql.ResultSet

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

    protected fun resultSetProducer(action: (PreparedStatement) -> ResultSet): ResultSetProducer = object : ResultSetProducer {
        override fun <R> withResultSet(block: (ResultSet) -> R): R = executeInternal { action(it).use(block) }
    }

    protected open fun createStatement(parsedSql: String): PreparedStatement {
        return statementCreator.createPreparedStatement(handle.getConnection(), parsedSql, context)
    }

    protected open fun applyQueryParameters(preparedStatement: PreparedStatement) {
        resolveParameters().forEach { (i, sqlArgument) ->
            sqlArgument.apply(i, preparedStatement)
        }
    }
}