package io.github.kotlinx.jdbc.tx

data class TransactionOptions(
    /**
     * Transaction Isolation level to be used for this transaction
     */
    val isolation: IsolationLevel? = null,
    /**
     * Is connection read only
     */
    val readOnly: Boolean? = null,
    /**
     * Exceptions for which the transaction should be rolled back. By default, all exceptions will cause a rollback.
     */
    val rollbackFor: Set<Class<out Throwable>> = mutableSetOf(),
    /**
     * Exceptions for which the transaction should not be rolled back. By default, no exceptions are excluded from rollback.
     */
    val noRollbackFor: Set<Class<out Throwable>> = mutableSetOf()
) {
    /**
     * Checks if the transaction should rolled back or not
     *
     * Any exception which cannot be classified as rollback or no-rollback will cause a rollback by default
     */
    fun shouldRollback(throwable: Throwable): Boolean {
        if (noRollbackFor.any { it.isAssignableFrom(throwable::class.java) }) return false
        if (rollbackFor.any { it.isAssignableFrom(throwable::class.java) }) return true
        return true
    }
}