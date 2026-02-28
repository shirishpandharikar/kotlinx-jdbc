package io.github.kotlinx.jdbc.core

import java.sql.ResultSet

fun interface RowMapper<T> {
    fun map(rs: ResultSet): T
}