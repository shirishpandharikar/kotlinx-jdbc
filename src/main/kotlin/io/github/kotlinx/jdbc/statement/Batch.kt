package io.github.kotlinx.jdbc.statement

interface Batch {
    fun add(sql: String): Batch
    fun execute(): IntArray
}