package io.github.kotlinx.jdbc.dsl

import io.github.kotlinx.jdbc.core.Handle
import io.github.kotlinx.jdbc.core.RowMapper

class Query internal constructor(handle: Handle, sql: String) : SqlStatement<Query>(handle, sql) {

    fun <T> map(mapper: RowMapper<T>): ResultIterable<T> {
        return ResultIterable(mapper)
    }

    /**
     * A lazy iterable that represents a pending database query execution.
     * This class does not hold data; it holds the *instructions* to fetch data.
     * The database query is only executed when one of the methods
     * (list, first, stream, etc.) is called.
     *
     * @param T The type of object each row will be mapped to.
     */
    inner class ResultIterable<T>(private val mapper: RowMapper<T>) {

        fun list(): List<T> {
            return executeInternal { pstmt ->
                pstmt.executeQuery().use { rs ->
                    buildList {
                        while (rs.next()) {
                            add(mapper.map(rs))
                        }
                    }
                }
            }
        }

        fun firstOrNull(): T? {
            return executeInternal { pstmt ->
                pstmt.executeQuery().use { rs ->
                    if (rs.next()) mapper.map(rs) else null
                }
            }
        }

        fun first(): T {
            return firstOrNull() ?: throw IllegalStateException("Expected at least 1 row, but got 0.")
        }

        fun one(): T {
            return executeInternal { pstmt ->
                pstmt.executeQuery().use {
                    check(it.next()) { "Expected 1 row, but got 0." }
                    val result = mapper.map(it)
                    check(!it.next()) { "Expected 1 row, but got more than 1." }
                    result
                }
            }
        }

        fun <R> fold(initial: R, operation: (R, T) -> R): R {
            return executeInternal { pstmt ->
                pstmt.executeQuery().use { rs ->
                    var accumulator = initial
                    while (rs.next()) {
                        accumulator = operation(accumulator, mapper.map(rs))
                    }
                    accumulator
                }
            }
        }
    }
}