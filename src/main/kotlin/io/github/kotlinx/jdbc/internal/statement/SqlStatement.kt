package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.spi.SqlArgument

interface SqlStatement<S: SqlStatement<S>> {
    fun bind(position: Int, value: Any): S
    fun bind(position: Int, value: SqlArgument): S
    fun bind(name: String, value: Any): S
    fun bind(name: String, value: SqlArgument): S
}