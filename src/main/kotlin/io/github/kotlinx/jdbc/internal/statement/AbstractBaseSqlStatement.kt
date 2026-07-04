package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext
import io.github.kotlinx.jdbc.internal.statement.named.ParsedSql
import io.github.kotlinx.jdbc.spi.SqlArgument

internal abstract class AbstractBaseSqlStatement<S : SqlStatement<S>>(protected val handle: Handle, protected val sql: String) : SqlStatement<S> {
    protected val registry = handle.sqlArgumentRegistry()
    protected val statementCreator = handle.statementCreator()
    protected val context = StatementContext()
    protected val bindings = Bindings()

    protected val parsedSql: ParsedSql by lazy { handle.parsedSqlCache().get(sql) }

    /**
     * Builds a map of 1-based JDBC positions to [SqlArgument]s, ready to be applied
     * to a [java.sql.PreparedStatement].
     *
     * - **Positional bindings** — each bound index maps directly to a `?` placeholder.
     *   The bindings are validated before use (see [validatePositionalParameters]).
     * - **Named bindings** — each name is looked up in the parsed SQL to find its
     *   placeholder positions. The same value is applied to every occurrence of the name.
     *
     * Entries are sorted by position once here, so callers always iterate in JDBC order.
     * Returns an empty map if no parameters were bound.
     *
     * @throws IllegalArgumentException if a named parameter is not found in the SQL,
     *   or if positional parameter validation fails.
     */
    protected fun resolveParameters(): Map<Int, SqlArgument> {
        val bound = when {
            bindings.hasPositional() -> {
                val positional = bindings.positionalValues()
                validatePositionalParameters(positional)
                positional.entries
                    .sortedBy { it.key }
                    .associate { (i, value) -> i to resolveToSqlArgument(value) }
            }
            bindings.hasNamed() -> {
                buildMap {
                    bindings.namedValues().forEach { (name, value) ->
                        val positions = parsedSql.parameterIndexesByName[name]
                            ?: throw IllegalArgumentException("No parameter named '$name' found in SQL")
                        positions.forEach { pos -> put(pos, resolveToSqlArgument(value)) }
                    }
                }
            }
            else -> emptyMap()
        }
        return bound
    }

    /**
     * Checks that every `?` placeholder in the SQL has exactly one bound value.
     *
     * Two things are checked:
     * 1. **Count** — the number of bound positions must match the number of `?`
     *    placeholders in the SQL.
     * 2. **No missing index** — every index from 1 to N must be present. A missing
     *    index means either a gap (e.g. 1 and 3 bound but not 2) or an out-of-range
     *    index (e.g. position 5 bound when there is only 1 placeholder).
     *
     * @param bound the positional bindings keyed by 1-based position.
     * @throws IllegalArgumentException if the count does not match, or if any index
     *   in `[1..N]` has no binding.
     */
    private fun validatePositionalParameters(bound: Map<Int, Any>) {
        val expected = parsedSql.unnamedParameterCount
        require(bound.size == expected) {
            "Expected $expected positional parameter(s) but got ${bound.size} for SQL: [${parsedSql.originalSql}]"
        }
        val missing = (1..expected).firstOrNull { it !in bound }
        require(missing == null) {
            "Missing positional parameter at index $missing for SQL: [${parsedSql.originalSql}]"
        }
    }

    private fun resolveToSqlArgument(value: Any): SqlArgument {
        return when (value) {
            is SqlArgument -> value  // Already resolved
            else -> registry.find(value)  // Convert raw value to SqlArgument
        }
    }
}

