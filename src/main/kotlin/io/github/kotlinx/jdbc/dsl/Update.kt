package io.github.kotlinx.jdbc.dsl

import io.github.kotlinx.jdbc.core.Handle

class Update internal constructor(handle: Handle, sql: String): SqlStatement<Update>(handle, sql) {

    fun execute(): Int = executeInternal { it.executeUpdate() }

}