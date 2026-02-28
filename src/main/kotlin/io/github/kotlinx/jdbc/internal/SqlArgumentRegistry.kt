package io.github.kotlinx.jdbc.internal

import io.github.kotlinx.jdbc.spi.sql.argument.SqlArgument
import io.github.kotlinx.jdbc.spi.sql.argument.factory.SqlArgumentFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class SqlArgumentRegistry {

    private val factories = CopyOnWriteArrayList<SqlArgumentFactory>()
    private val cache = ConcurrentHashMap<Class<*>, SqlArgumentFactory>()

    init {
        register(EssentialArgumentFactory())
        register(LobArgumentFactory())
    }

    fun register(factory: SqlArgumentFactory) {
        factories.add(0, factory)
        cache.clear()
    }

    /**
     * Resolves the strategy for a NON-NULL value.
     * strictly throws if no factory is registered for the type.
     */
    fun find(value: Any): SqlArgument {
        // 1. Resolve Factory (O(1) Cached)
        // computeIfAbsent will propagate the exception from findFactoryFor if not found.
        val factory = cache.computeIfAbsent(value::class.java) { _ -> findFactoryFor(value) }

        // 2. Build Argument
        // We enforce strictness here too: The factory claimed the type during lookup,
        // so it must return a valid Argument now.
        return factory.build(value)
            ?: throw IllegalStateException(
                "Factory '${factory::class.simpleName}' claimed support for type '${value::class.simpleName}' " +
                        "but returned null during binding."
            )
    }

    /**
     * Scans registered factories.
     * Throws exception if no match is found (No Fallback).
     */
    private fun findFactoryFor(value: Any): SqlArgumentFactory {
        for (factory in factories) {
            if (factory.build(value) != null) {
                return factory
            }
        }

        //Fail fast.
        throw IllegalArgumentException(
            "No ArgumentFactory registered for type '${value::class.qualifiedName}'. " +
                    "Please register a SqlArgumentFactory."
        )
    }

}