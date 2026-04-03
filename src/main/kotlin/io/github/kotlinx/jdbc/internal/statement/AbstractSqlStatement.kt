package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.argument.SqlArgumentRegistry
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.sql.PreparedStatement
import java.util.TreeMap

internal abstract class AbstractSqlStatement<S : SqlStatement<S>>(handle: Handle, sql: String) : AbstractBaseSqlStatement<S>(handle, sql) {

    private val positionalParams = TreeMap<Int, SqlArgument>()

    //TODO [Handle] should provide
    private val registry = SqlArgumentRegistry()

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

    protected fun <R> executeInternal(action: (PreparedStatement) -> R): R {

        val parsedSql = parseSql()

        val pstmt = createStatement(parsedSql)

        return pstmt.use {
            if (positionalParams.isNotEmpty()) {
                positionalParams.forEach { (i, binding) ->
                    binding.apply(i, it)
                }
            }
            action(it)
        }
    }

    protected open fun createStatement(parsedSql: String): PreparedStatement {
        return getStatementCreator().createPreparedStatement(handle.getConnection(), parsedSql, getContext())
    }

    private fun parseSql(): String {
        return this.sql
    }
}