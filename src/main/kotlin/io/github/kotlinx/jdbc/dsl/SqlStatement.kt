package io.github.kotlinx.jdbc.dsl

import io.github.kotlinx.jdbc.core.Handle
import io.github.kotlinx.jdbc.internal.SqlArgumentRegistry
import io.github.kotlinx.jdbc.spi.sql.argument.SqlArgument
import java.sql.PreparedStatement
import java.util.TreeMap

abstract class SqlStatement<S: SqlStatement<S>>(private val handle: Handle, private val sql: String) {

    /**
     * Positional parameters.
     */
    protected val positionalParams = TreeMap<Int, Positional>()

    private val registry = SqlArgumentRegistry()

    @Suppress("UNCHECKED_CAST")
    fun bind(position: Int, value: Any): S {
        positionalParams[position] = Positional(registry.find(value))
        return this as S
    }

    @Suppress("UNCHECKED_CAST")
    fun bind(position: Int, value: SqlArgument): S {
        positionalParams[position] = Positional(value)
        return this as S
    }

    protected fun <R> executeInternal(action: (PreparedStatement) -> R): R {
        val conn = handle.getConnection()
        val pstmt = conn.prepareStatement(sql)

        bindPositionalParams(pstmt)

        return action(pstmt)
    }

    private fun bindPositionalParams(pstmt: PreparedStatement) {
        positionalParams.forEach { (i, binding) ->
            binding.value.apply(i, pstmt)
        }
    }

    protected data class Positional(val value: SqlArgument)
    protected data class Named(val values: List<SqlArgument>)
}

