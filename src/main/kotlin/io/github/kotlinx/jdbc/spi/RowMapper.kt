package io.github.kotlinx.jdbc.spi

import java.sql.ResultSet

fun interface RowMapper<T> {
    fun map(rs: ResultSet): T
}