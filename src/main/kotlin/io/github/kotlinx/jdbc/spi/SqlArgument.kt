package io.github.kotlinx.jdbc.spi

import java.sql.PreparedStatement

/**
 * Functional interface representing a SQL argument
 *
 * This allows us to abstract away the binding logic and support various types, including custom ones.
 *
 * ```
 * class UUIDArgument(private val uuid: UUID): SqlArgument] {
 *   override fun apply(position: Int, pstmt: PreparedStatement) {
 *      statement.setString(parameterIndex, uuid.toString())
 *   }
 * }
 * ```
 *
 */
fun interface SqlArgument {
    fun apply(position: Int, pstmt: PreparedStatement)
}