package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext
import io.github.kotlinx.jdbc.internal.statement.named.NamedParameterParser
import io.github.kotlinx.jdbc.internal.statement.named.ParsedSql
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.util.TreeMap

internal abstract class AbstractBaseSqlStatement<S : SqlStatement<S>>(handle: Handle, protected val sql: String) : SqlStatement<S> {
    protected val registry = handle.sqlArgumentRegistry()
    protected val statementCreator = handle.statementCreator()
    protected val connection = handle.getConnection()
    protected val context = StatementContext()
    protected val bindings = Bindings()

    //TODO improve this by caching ParsedSql against the SQL
    protected val parsedSql: ParsedSql by lazy { NamedParameterParser.parse(sql) }

    protected fun resolveParameters(): TreeMap<Int, SqlArgument> {
        val result = TreeMap<Int, SqlArgument>()
        when {
            bindings.hasPositional() -> {
                bindings.positionalValues().forEach { (i, value) ->
                    result[i] = resolveToSqlArgument(value)
                }
            }
            bindings.hasNamed() -> {
                bindings.namedValues().forEach { (name, value) ->
                    val positions = parsedSql.parameterIndexesByName[name]
                        ?: throw IllegalArgumentException("No parameter named '$name' found in SQL")
                    positions.forEach { pos ->
                        result[pos] = resolveToSqlArgument(value)
                    }
                }
            }
        }
        return result
    }

    private fun resolveToSqlArgument(value: Any): SqlArgument {
        return when (value) {
            is SqlArgument -> value  // Already resolved
            else -> registry.find(value)  // Convert raw value to SqlArgument
        }
    }
}

