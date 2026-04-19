package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.Handle
import io.github.kotlinx.jdbc.internal.statement.ctx.StatementContext

internal abstract class AbstractBaseSqlStatement<S : SqlStatement<S>>(handle: Handle, protected val sql: String) : SqlStatement<S> {
    protected val registry = handle.sqlArgumentRegistry()
    protected val statementCreator = handle.statementCreator()
    protected val connection = handle.getConnection()
    protected val context = StatementContext()
}

