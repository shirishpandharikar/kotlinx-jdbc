package io.github.kotlinx.jdbc.internal.statement.factory

import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

/**
 * Factory for creating [Statement]s, [PreparedStatement]s and [CallableStatement]s.
 * This is used by the SQL statement classes to create the appropriate statement type based on the context.
 */
internal class StatementCreatorFactory : StatementCreator {

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