package io.github.kotlinx.jdbc.internal.statement.named

/**
 * Immutable result of parsing SQL with named parameters into JDBC positional SQL.
 */
data class ParsedSql(
    val originalSql: String,
    val jdbcSql: String,
    val parameterNamesByOccurrence: List<String>,
    val parameterIndexesByName: Map<String, IntArray>,
    val namedParameterCount: Int,
    val unnamedParameterCount: Int,
    val totalParameterCount: Int
)