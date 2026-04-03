package io.github.kotlinx.jdbc.spi

/**
 * Contract for factories that create [SqlArgument]s. This allows us to have a flexible and extensible way to create [SqlArgument]s for different types.
 * Each factory can support multiple types and encapsulate the logic for creating the appropriate [SqlArgument] for a given value.
 */
fun interface SqlArgumentFactory {
    /**
     * Creates an [SqlArgument] for the given value if this factory supports its type, or returns null if it does not.
     * This function is used during the [SqlArgument] resolution process to determine which factory can handle a given value.
     * @param value The value for which to create an Argument. This can be of any type.
     * @return A [SqlArgument] if this factory supports the type of the value, or null if it does not.
     */
    fun create(value: Any): SqlArgument?
}