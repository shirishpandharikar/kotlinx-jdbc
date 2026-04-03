package io.github.kotlinx.jdbc

import javax.sql.DataSource

class Kdbi(private val dataSource: DataSource) {
    /**
     * Executes the given block returns the result.
     * @param block The block of code to execute with the handle.
     * @return The result of the block execution.
     */
    fun <R> withHandle(block: Handle.() -> R): R = Handle(dataSource).use { return it.block() }
    /**
     * Executes the given block without returning a result.
     * @param block The block of code to execute with the handle.
     * @see withHandle
     */
    fun useHandle(block: Handle.() -> Unit) = withHandle { block() }
    /**
     * Executes the given block within a transaction and returns the result.
     * Commits the transaction if the block completes successfully, otherwise the entire transaction is rolled back
     * @param block The block of code to execute within a transaction
     * @return R Result of the block execution
     */
    fun <R> withTransaction(block: Handle.() -> R): R = Handle(dataSource).use { it.withTransaction { block() } }
    /**
     * Executes the given block within a transaction without returning a result.
     * Commits the transaction if the block completes successfully, otherwise the entire transaction is rolled back
     * @param block The block of code to execute within a transaction
     * @see withTransaction
     */
    fun useTransaction(block: Handle.() -> Unit) = withTransaction { block() }
}