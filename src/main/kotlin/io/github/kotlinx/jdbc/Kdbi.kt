package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.argument.SqlArgumentRegistry
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreator
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreatorFactory
import javax.sql.DataSource

class Kdbi(internal val dataSource: DataSource) {

    private val sqlArgumentRegistry = SqlArgumentRegistry()
    private val statementCreatorFactory = StatementCreatorFactory()

    /**
     * Executes the given block returns the result.
     * @param block The block of code to execute with the handle.
     * @return The result of the block execution.
     */
    fun <R> withHandle(block: Handle.() -> R): R = Handle(this).use { return it.block() }
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
    fun <R> withTransaction(block: Handle.() -> R): R = Handle(this).use { it.withTransaction { block() } }
    /**
     * Executes the given block within a transaction without returning a result.
     * Commits the transaction if the block completes successfully, otherwise the entire transaction is rolled back
     * @param block The block of code to execute within a transaction
     * @see withTransaction
     */
    fun useTransaction(block: Handle.() -> Unit) = withTransaction { block() }

    internal fun sqlArgumentRegistry(): SqlArgumentRegistry = sqlArgumentRegistry
    internal fun statementCreator(): StatementCreator = statementCreatorFactory
}