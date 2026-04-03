package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

abstract class AbstractBaseSqlStatement<S : SqlStatement<S>>(protected val handle: Handle, protected val sql: String) :
    SqlStatement<S> {

    private val context = StatementContext()
    // [Handle] should provide
    private val statementCreatorFactory = StatementCreatorFactory()

    protected fun getContext(): StatementContext = this.context
    protected fun getStatementCreator(): StatementCreator = this.statementCreatorFactory

    protected class StatementContext {

        private var returnGeneratedKeys: Boolean = false
        private var generatedKeyColumns: List<String> = emptyList()

        // Read-only view
        fun shouldReturnGeneratedKeys(): Boolean = returnGeneratedKeys
        fun getGeneratedKeyColumns(): List<String> = generatedKeyColumns

        fun useGeneratedKeys(vararg generatedKeyColumns: String) {
            require(generatedKeyColumns.isNotEmpty()) { "At least one generated key column is required" }
            this.returnGeneratedKeys = true
            this.generatedKeyColumns = generatedKeyColumns.toList()
        }
    }

    protected interface StatementCreator {
        fun createStatement(connection: Connection, statementContext: StatementContext): Statement
        fun createPreparedStatement(connection: Connection, sql: String, statementContext: StatementContext): PreparedStatement
        fun createCallableStatement(connection: Connection, sql: String, statementContext: StatementContext): CallableStatement
    }

    protected class StatementCreatorFactory: StatementCreator {

        override fun createStatement(connection: Connection, statementContext: StatementContext): Statement {
            return connection.createStatement()
        }

        override fun createPreparedStatement(connection: Connection, sql: String, statementContext: StatementContext): PreparedStatement {
            return if (statementContext.shouldReturnGeneratedKeys()) {
                val generatedKeyColumns = statementContext.getGeneratedKeyColumns()
                if (generatedKeyColumns.isEmpty()) {
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                } else {
                    connection.prepareStatement(sql, generatedKeyColumns.toTypedArray())
                }
            } else {
                connection.prepareStatement(sql)
            }
        }

        override fun createCallableStatement(connection: Connection, sql: String, statementContext: StatementContext): CallableStatement {
            return connection.prepareCall(sql)
        }

    }

}

