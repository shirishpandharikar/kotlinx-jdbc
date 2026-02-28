package io.github.kotlinx.jdbc.core

import javax.sql.DataSource

class Dbi(private val dataSource: DataSource) {

    /**
     * Executes the given block returns the result.
     * @param block The block of code to execute with the handle.
     * @return The result of the block execution.
     */
    fun <R> withHandle(block: Handle.() -> R): R {
        Handle(dataSource).use { return it.block() }
    }

    /**
     * Executes the given block without returning a result.
     * @param block The block of code to execute with the handle.
     */
    fun useHandle(block: Handle.() -> Unit) = withHandle { block() }

}