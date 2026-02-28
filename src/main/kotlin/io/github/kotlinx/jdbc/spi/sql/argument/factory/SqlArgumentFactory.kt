package io.github.kotlinx.jdbc.spi.sql.argument.factory

import io.github.kotlinx.jdbc.spi.sql.argument.SqlArgument

/**
 * Factory for creating [SqlArgument] instances from given values. Once registered, [SqlArgumentFactory] is cached for reuse
 * The below example demonstrates how to implement a custom [SqlArgumentFactory] for handling UUID values
 *
 * ```kotlin
 * class UUIDSqlArgumentFactory : SqlArgumentFactory {
 *     override fun build(value: Any): SqlArgument? {
 *          return SqlArgument { index, pstmt ->
 *              pstmt.setString(index, value.toString())
 *          }
 *     }
 * }
 * ```
 * [SqlArgumentFactory] can produce different [SqlArgument] depending on the value type
 *
 * ```kotlin
 * class LobSqlArgumentFactory: SqlArgumentFactory {
 *   override fun build(value: Any): SqlArgument? {
 *     return when (value) {
 *       is Clob -> SqlArgument { index, pstmt -> pstmt.setClob(index, value) }
 *       is Blob -> SqlArgument { index, pstmt -> pstmt.setBlob(index, value) }
 *       else -> null
 *     }
 *   }
 * }
 * ```
 *
 */
fun interface SqlArgumentFactory {
    /**
     * Build a [SqlArgument] for a given value
     * @return [SqlArgument] if a value can be handled else `null`
     */
    fun build(value: Any): SqlArgument?
}