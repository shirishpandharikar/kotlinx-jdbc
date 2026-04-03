package io.github.kotlinx.jdbc.statement

import io.github.kotlinx.jdbc.internal.statement.ResultIterable
import io.github.kotlinx.jdbc.internal.statement.SqlStatement
import io.github.kotlinx.jdbc.spi.RowMapper

interface Update : SqlStatement<Update> {
    fun execute(): Int
    fun <T> executeWithGeneratedKeys(vararg generateKeyColumns: String, mapper: RowMapper<T>): ResultIterable<T>
}


