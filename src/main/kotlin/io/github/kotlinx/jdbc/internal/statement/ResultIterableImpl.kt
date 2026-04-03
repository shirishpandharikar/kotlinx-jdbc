package io.github.kotlinx.jdbc.internal.statement

import io.github.kotlinx.jdbc.spi.RowMapper
import kotlin.collections.buildList

internal class ResultIterableImpl<T>(private val mapper: RowMapper<T>, private val resultSetProducer: ResultSetProducer) : ResultIterable<T> {

    override fun list(): List<T> {
        return resultSetProducer.withResultSet {
            buildList {
                while (it.next()) {
                    add(mapper.map(it))
                }
            }
        }
    }

    override fun first(): T {
        return resultSetProducer.withResultSet {
            checkElement(it.next()) { "Expected at least one row but found none" }
            val result = mapper.map(it)
            checkElement(!it.next()) { "Expected exactly one row but found more" }
            result
        }
    }

    override fun firstOrNull(): T? {
        return resultSetProducer.withResultSet {
            if (!it.next()) {
                null
            } else {
                val result = mapper.map(it)
                check(!it.next()) { "Expected exactly one row but found more" }
                result
            }
        }
    }

    override fun <K> associateBy(keySelector: (T) -> K): Map<K, T> {
        TODO("Not yet implemented")
    }

    override fun <K, V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> {
        TODO("Not yet implemented")
    }

    private inline fun checkElement(value: Boolean, lazyMessage: () -> Any) {
        if (!value) throw NoSuchElementException(lazyMessage().toString())
    }

}