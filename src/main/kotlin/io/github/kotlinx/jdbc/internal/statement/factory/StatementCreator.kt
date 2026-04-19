package io.github.kotlinx.jdbc.internal.statement.factory

import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext
import java.sql.CallableStatement
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

/**
 * Factory interface for creating [Statement]s, [PreparedStatement]s and [CallableStatement]s.
 * @see StatementCreatorFactory
 */
interface StatementCreator {
    /**
     * Create a [Statement]. This is used for statements that do not require parameters, such as DDL statements.
     * @param connection JDBC connection for which the [Statement] is created for
     * @param statementContext Contextual data to be used when creating [Statement]
     * @return [Statement]
     * @see StatementContext
     */
    fun createStatement(connection: Connection, statementContext: StatementContext): Statement
    /**
     * Create a [PreparedStatement]. This is used for statements that require parameters
     * @param connection JDBC connection for which the [PreparedStatement] is created for
     * @param sql Translated SQL which should be prepared
     * @param statementContext Contextual data to be used when creating [Statement]
     * @return [PreparedStatement]
     * @see StatementContext
     */
    fun createPreparedStatement(connection: Connection, sql: String, statementContext: StatementContext): PreparedStatement
    /**
     * Create a [CallableStatement]. This is used for stored procedure calls
     * @param connection JDBC connection for which the [CallableStatement] is created for
     * @param sql Translated SQL which should be prepared
     * @param statementContext Contextual data to be used when creating [Statement]
     * @return [PreparedStatement]
     * @see StatementContext
     */
    fun createCallableStatement(connection: Connection, sql: String, statementContext: StatementContext): CallableStatement
}