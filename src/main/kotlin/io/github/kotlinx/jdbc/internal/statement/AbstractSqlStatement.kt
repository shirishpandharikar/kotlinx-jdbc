package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.sql.PreparedStatement
import java.util.*

internal abstract class AbstractSqlStatement<S : SqlStatement<S>>(handle: Handle, sql: String) : AbstractBaseSqlStatement<S>(handle, sql) {

    protected val positionalParams = TreeMap<Int, SqlArgument>()
    protected val namedParams = linkedMapOf<String, SqlArgument>()

    @Suppress("UNCHECKED_CAST")
    override fun bind(position: Int, value: Any): S {
        positionalParams[position] = registry.find(value)
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(position: Int, value: SqlArgument): S {
        positionalParams[position] = value
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(name: String, value: Any): S {
        namedParams[name] = registry.find(value)
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    override fun bind(name: String, value: SqlArgument): S {
        namedParams[name] = value
        return this as S
    }

    protected fun <R> executeInternal(action: (PreparedStatement) -> R): R {

        val parsedSql = parseSql()

        val pstmt = createStatement(parsedSql)

        return pstmt.use {
            applyQueryParameters(it)
            action(it)
        }
    }

    protected open fun createStatement(parsedSql: String): PreparedStatement {
        return statementCreator.createPreparedStatement(connection, parsedSql, context)
    }

    protected open fun applyQueryParameters(preparedStatement: PreparedStatement) {
        if (positionalParams.isNotEmpty()) {
            positionalParams.forEach { (i, binding) ->
                binding.apply(i, preparedStatement)
            }
        }
    }

    private fun parseSql(): String {
        return this.sql
    }
}