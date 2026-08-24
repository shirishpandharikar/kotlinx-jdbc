package io.github.kotlinx.jdbc.internal.statement.ctx

class StatementContext {

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