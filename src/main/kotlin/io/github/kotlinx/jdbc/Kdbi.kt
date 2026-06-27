package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.argument.SqlArgumentRegistry
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreator
import io.github.kotlinx.jdbc.internal.statement.factory.StatementCreatorFactory
import io.github.kotlinx.jdbc.internal.statement.named.NamedParameterParser
import io.github.kotlinx.jdbc.internal.statement.named.cache.ParsedSqlCache
import io.github.kotlinx.jdbc.tx.TransactionOptions
import javax.sql.DataSource

class Kdbi(internal val dataSource: DataSource) {

    private val sqlArgumentRegistry = SqlArgumentRegistry()
    private val statementCreatorFactory = StatementCreatorFactory()
    private val parsedSqlCache = ParsedSqlCache(maxSize = ParsedSqlCache.DEFAULT_MAX_SIZE, parser = NamedParameterParser::parse)

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
     * Commits the transaction if the block completes successfully, otherwise the entire transaction is rolled back.
     * Uses default transaction options to execute the transaction. Any exception causes the transaction to be rolled back.
     * @param block The block of code to execute within a transaction
     * @return R Result of the block execution
     * @see TransactionOptions
     */
    fun <R> inTransaction(block: Handle.() -> R): R = inTransaction(TransactionOptions(), block)
    /**
     * Executes the given block within a transaction and returns the result.
     * Uses the given [TransactionOptions] to execute the transaction.
     * @param options [TransactionOptions] to execute the transaction with
     * @param block The block of code to execute within a transaction
     * @return R Result of the block execution
     * @see TransactionOptions
     */
    fun <R> inTransaction(options: TransactionOptions, block: Handle.() -> R): R = Handle(this).use { it.inTransaction(options) { block() } }
    /**
     * Executes the given block within a transaction without returning a result.
     * Commits the transaction if the block completes successfully, otherwise the entire transaction is rolled back.
     * Uses default transaction options to execute the transaction. Any exception causes the transaction to be rolled back.
     * @param block The block of code to execute within a transaction
     * @see inTransaction
     * @see TransactionOptions
     */
    fun useTransaction(block: Handle.() -> Unit) = useTransaction (TransactionOptions()) { block() }
    /**
     * Executes the given block within a transaction without returning a result.
     * Uses the given [TransactionOptions] to execute the transaction.
     * @param options [TransactionOptions] to execute the transaction with
     * @param block The block of code to execute within a transaction
     * @see inTransaction
     * @see TransactionOptions
     */
    fun useTransaction(options: TransactionOptions, block: Handle.() -> Unit) = inTransaction(options) { block() }

    internal fun sqlArgumentRegistry(): SqlArgumentRegistry = sqlArgumentRegistry
    internal fun statementCreator(): StatementCreator = statementCreatorFactory
    internal fun parsedSqlCache(): ParsedSqlCache = parsedSqlCache
}