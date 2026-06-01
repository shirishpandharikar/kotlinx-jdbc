package io.github.kotlinx.jdbc.tx

import java.sql.Connection
import java.sql.Savepoint
import java.util.UUID
import kotlin.collections.ArrayDeque

internal class TransactionContext {
    var depth: Int = 0
        private set

    private val savepoints = ArrayDeque<Savepoint>()

    // Original connection state (captured at outermost begin)
    private var originalAutoCommit: Boolean? = null
    private var originalIsolation: Int? = null
    private var originalReadOnly: Boolean? = null

    fun begin(connection: Connection, options: TransactionOptions) {
        if (depth == 0) {
            // Capture originals
            originalAutoCommit = connection.autoCommit
            originalIsolation = connection.transactionIsolation
            originalReadOnly = connection.isReadOnly

            // Apply options
            connection.autoCommit = false
            options.isolation?.let { connection.transactionIsolation = it.level }
            options.readOnly?.let { connection.isReadOnly = it }
        } else {
            // Create savepoint for nested transaction
            val savepoint = connection.setSavepoint("SAVEPOINT_${depth}_${UUID.randomUUID()}")
            savepoints.addLast(savepoint)
        }
        depth++
    }

    fun commit(connection: Connection) {
        check(depth > 0) { "No active transaction to commit" }
        depth--
        if (depth == 0) {
            // Outermost transaction
            connection.commit()
        } else {
            // Nested transaction - release savepoint
            val savepoint = savepoints.removeLast()
            // some drivers may thrown an exception handle and ignore
            connection.releaseSavepoint(savepoint)
        }
    }

    fun rollback(connection: Connection) {
        check(depth > 0) { "No active transaction to rollback" }
        depth--
        if (depth == 0) {
            connection.rollback()
        } else {
            // Rollback to savepoint (nested failure)
            val sp = savepoints.removeLast()
            connection.rollback(sp)
        }
    }

    fun end(connection: Connection) {
        if (depth == 0) {
            // Restore original connection state
            originalAutoCommit?.let { connection.autoCommit = it }
            originalIsolation?.let { connection.transactionIsolation = it }
            originalReadOnly?.let { connection.isReadOnly = it }
            reset()
        }
    }

    private fun reset() {
        originalAutoCommit = null
        originalIsolation = null
        originalReadOnly = null
        savepoints.clear()
    }
}