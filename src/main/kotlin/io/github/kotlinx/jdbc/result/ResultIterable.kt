package io.github.kotlinx.jdbc.result

/**
 * A lazy, single-use view over the rows of an executed SQL statement.
 *
 * A [ResultIterable] does not execute anything on creation. Instead, each **terminal
 * operation** such as [list], [first], [associateBy] triggers
 * execution of the underlying statement, iterates the resulting [java.sql.ResultSet], and
 * closes it before returning.
 *
 * A [ResultIterable] is backed by a live database cursor and is intended to be consumed by
 * **exactly one** terminal operation. Invoking a second terminal operation on the same
 * instance is not supported and will fail fast. To read the data again, re-run the query to
 * obtain a fresh [ResultIterable].
 *
 * Each row is converted to [T] by the [io.github.kotlinx.jdbc.spi.RowMapper] (or
 * [io.github.kotlinx.jdbc.spi.ColumnMapper]) supplied when the result was created.
 *
 * @param T the type each row is mapped to
 */
interface ResultIterable<T> {
    /**
     * Executes the statement and returns **all** rows as a [List], in result-set order.
     *
     * The returned list is a fully materialized snapshot; the underlying
     * [java.sql.ResultSet] is closed before this method returns.
     *
     * @return a list of all mapped rows, or an empty list if the result has no rows
     * @throws java.sql.SQLException if statement execution or row mapping fails
     */
    fun list(): List<T>

    /**
     * Executes the statement and returns the **first** row, ignoring any remaining rows.
     *
     * Use this when "the first row" is sufficient and additional rows should be discarded.
     * To assert that the result contains a single row, use [one] instead.
     *
     * @return the first mapped row
     * @throws io.github.kotlinx.jdbc.exception.EmptyResultException if the result has no rows
     * @throws java.sql.SQLException if statement execution or row mapping fails
     */
    fun first(): T

    /**
     * Executes the statement and returns the **first** row, or `null` if the result is empty.
     *
     * Like [first], any rows after the first are ignored. This never throws for an empty
     * result; it returns `null` instead.
     *
     * @return the first mapped row, or `null` if the result has no rows
     * @throws java.sql.SQLException if statement execution or row mapping fails
     */
    fun firstOrNull(): T?

    /**
     * Executes the statement and returns **exactly one** row.
     *
     * Unlike [first], this enforces that the result contains a single row: an empty result
     * or a result with more than one row is treated as an error. At most two rows are read
     * from the cursor to detect the "more than one" case.
     *
     * @return the single mapped row
     * @throws io.github.kotlinx.jdbc.exception.EmptyResultException if the result has no rows
     * @throws io.github.kotlinx.jdbc.exception.TooManyRowsException if the result has more than one row
     * @throws java.sql.SQLException if statement execution or row mapping fails
     * @see oneOrNull
     * @see first
     */
    fun one(): T

    /**
     * Executes the statement and returns **at most one** row, or `null` if the result is empty.
     *
     * Like [one], more than one row is treated as an error, but an empty result yields `null`
     * rather than throwing. At most two rows are read from the cursor to detect the
     * "more than one" case.
     *
     * @return the single mapped row, or `null` if the result has no rows
     * @throws io.github.kotlinx.jdbc.exception.TooManyRowsException if the result has more than one row
     * @throws java.sql.SQLException if statement execution or row mapping fails
     * @see one
     * @see firstOrNull
     */
    fun oneOrNull(): T?

    /**
     * Executes the statement and returns a [Map] of rows keyed by [keySelector].
     *
     * Each row becomes a value in the map, associated with the key derived from it. If
     * multiple rows produce the same key, the **last** row encountered (in result-set order)
     * wins, consistent with [kotlin.collections.associateBy].
     *
     * @param K the key type
     * @param keySelector derives the map key from each mapped row
     * @return a map of mapped rows keyed by [keySelector]; empty if the result has no rows
     * @throws java.sql.SQLException if statement execution or row mapping fails
     */
    fun <K> associateBy(keySelector: (T) -> K): Map<K, T>

    /**
     * Executes the statement and returns a [Map] keyed by [keySelector] with values produced
     * by [valueTransform].
     *
     * For each row, [keySelector] derives the key and [valueTransform] derives the value. If
     * multiple rows produce the same key, the **last** row encountered (in result-set order)
     * wins, consistent with [kotlin.collections.associateBy].
     *
     * @param K the key type
     * @param V the value type
     * @param keySelector derives the map key from each mapped row
     * @param valueTransform derives the map value from each mapped row
     * @return a map keyed by [keySelector] with values from [valueTransform]; empty if the result has no rows
     * @throws java.sql.SQLException if statement execution or row mapping fails
     */
    fun <K, V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V>
}