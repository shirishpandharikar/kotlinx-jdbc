package io.github.kotlinx.jdbc.spi

/**
 * An interface that represents a [SqlArgument] that can be logged as a string.
 * Custom [SqlArgument] implementations can implement this interface to provide a string representation of the [SqlArgument] value for logging purposes.
 *
 * ```
 * class UuidArgument(private val uuid: UUID): SqlArgument, Loggable {
 *     override fun apply(index: Int, pstmt: PreparedStatement) {
 *         pstmt.setString(index, uuid.toString())
 *     }
 *     override fun log(): String {
 *         return "UUID('${uuid.toString()}')"
 *     }
 * }
 * ```
 */
fun interface Loggable {
    fun log(): String
}