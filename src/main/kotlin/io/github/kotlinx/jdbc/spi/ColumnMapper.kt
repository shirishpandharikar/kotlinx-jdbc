package io.github.kotlinx.jdbc.spi

import java.sql.ResultSet

/**
 * Maps a [ResultSet] column to a type [T]
 *
 * ```
 * enum class Status { "ACTIVE", "INACTIVE" }
 *
 * val statusMapper = ColumnMapper { rs, columnIndex ->
 *      Status.valueOf(rs.getString(columnIndex))
 * }
 * ```
 */
fun interface ColumnMapper<T> {
    /**
     * Maps a column from the [ResultSet] to an object of type [T]
     *
     * @param resultSet The [ResultSet] containing the data
     * @param columnIndex The column index (1-based) to map
     * @return Type [T] representing the mapped value
     */
    fun map(resultSet: ResultSet, columnIndex: Int): T
    /**
     * Maps a column from the [ResultSet] to an object of type [T].
     * This method internally finds the index of the column using the provided column name
     * and delegates to the other [map] method.
     *
     * @param resultSet The [ResultSet] containing the data
     * @param columnName The column label to use
     * @return Type [T] representing the mapped value
     */
    fun map(resultSet: ResultSet, columnName: String): T = map(resultSet, resultSet.findColumn(columnName))
}