package io.github.kotlinx.jdbc.internal.statement

import java.util.TreeMap

internal class Bindings {
    private val positional = TreeMap<Int, Any>()
    private val named = linkedMapOf<String, Any>()

    private enum class Mode {
        UNSET, POSITIONAL, NAMED
    }

    /**
     * Current binding mode. Default value is `UNSET`
     * @see Mode
     */
    private var mode = Mode.UNSET

    /**
     * Add a positional parameter
     */
    fun addPositional(position: Int, value: Any) {
        validate(Mode.POSITIONAL)
        positional[position] = value
    }

    /**
     * Add a named parameter
     */
    fun addNamed(name: String, value: Any) {
        validate(Mode.NAMED)
        named[name] = value
    }

    /**
     * Returns copy of the positional parameters
     */
    fun positionalValues(): TreeMap<Int, Any> = TreeMap(positional)

    /**
     * Returns copy of the named parameters
     */
    fun namedValues(): LinkedHashMap<String, Any> = LinkedHashMap(named)

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
     * Validates if a single binding mode is being used
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