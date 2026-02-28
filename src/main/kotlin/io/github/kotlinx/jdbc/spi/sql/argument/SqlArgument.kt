package io.github.kotlinx.jdbc.spi.sql.argument

import java.sql.PreparedStatement

/**
 * Interface which determines how to set a specific SQL argument in a [PreparedStatement]
 * The below example demonstrates how to implement a custom [SqlArgument] for setting a InputStream value.
 *
 * ```kotlin
 * class AsciiStreamSqlArgument: SqlArgument {
 *     fun apply(position: Int, pstmt: PreparedStatement) {
 *         pstmt.setAsciiStream(position, ins, len)
 *     }
 * }
 * ```
 */
fun interface SqlArgument {
    fun apply(position: Int, pstmt: PreparedStatement)
}