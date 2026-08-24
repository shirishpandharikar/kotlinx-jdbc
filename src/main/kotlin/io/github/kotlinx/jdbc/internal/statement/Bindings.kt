package io.github.kotlinx.jdbc.internal.statement

internal class Bindings {
    private val positional = HashMap<Int, Any>()
    private val named = linkedMapOf<String, Any>()

    private enum class Mode {
        UNSET, POSITIONAL, NAMED
    }

    /**
     * Current binding mode, starts as [Mode.UNSET] and is locked to either
     * [Mode.POSITIONAL] or [Mode.NAMED] on the first call to [addPositional] or [addNamed].
     */
    private var mode = Mode.UNSET

    /**
     * Adds a value for the `?` placeholder at the given [position].
     *
     * Positions are 1-based to match JDBC. If the same [position] is bound twice,
     * the last value wins.
     *
     * @param position 1-based index of the `?` placeholder.
     * @param value    the value to bind.
     * @throws IllegalArgumentException if [position] is less than 1, or if named
     *   parameters have already been added.
     */
    fun addPositional(position: Int, value: Any) {
        require(position >= 1) { "Positional parameter index must be >= 1, got $position" }
        validate(Mode.POSITIONAL)
        positional[position] = value
    }

    /**
     * Adds a value for the named parameter [name].
     *
     * @param name  the parameter name as written in the SQL (without the leading `:`).
     * @param value the value to bind.
     * @throws IllegalArgumentException if positional parameters have already been added.
     */
    fun addNamed(name: String, value: Any) {
        validate(Mode.NAMED)
        named[name] = value
    }

    /**
     * Returns the positional bindings, keyed by 1-based position.
     */
    fun positionalValues(): Map<Int, Any> = positional

    /**
     * Returns the named bindings, preserving insertion order.
     */
    fun namedValues(): Map<String, Any> = named

    /**
     * Returns `true` if the [Bindings] has named parameters, else `false`
     */
    fun hasNamed(): Boolean = named.isNotEmpty()

    /**
     * Returns `true` if the [Bindings] has positional parameters, else `false`
     */
    fun hasPositional(): Boolean = positional.isNotEmpty()

    fun isEmpty(): Boolean = positional.isEmpty() && named.isEmpty()

    fun clear() {
        positional.clear()
        named.clear()
    }

    /**
     * Ensures that positional and named parameters are never mixed on the same statement.
     * The mode is set on the first binding and all subsequent bindings must match it.
     *
     * @throws IllegalArgumentException if [newMode] conflicts with the already-set mode.
     */
    private fun validate(newMode: Mode) {
        when (mode) {
            Mode.UNSET -> mode = newMode
            Mode.POSITIONAL -> require(newMode != Mode.NAMED) {
                "Cannot mix positional and named parameters"
            }

            Mode.NAMED -> require(newMode != Mode.POSITIONAL) {
                "Cannot mix positional and named parameters"
            }
        }
    }
}