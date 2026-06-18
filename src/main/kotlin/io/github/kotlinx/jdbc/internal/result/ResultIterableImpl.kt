package io.github.kotlinx.jdbc.internal.result

import io.github.kotlinx.jdbc.spi.RowMapper

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

    override fun <K> associateBy(keySelector: (T) -> K): Map<K, T> = associateBy(keySelector) { it }

    override fun <K, V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V> {
        return resultSetProducer.withResultSet {
            buildMap {
                while (it.next()) {
                    val item = mapper.map(it)
                    put(keySelector(item), valueTransform(item))
                }
            }
        }
    }

    private inline fun checkElement(value: Boolean, lazyMessage: () -> Any) {
        if (!value) throw NoSuchElementException(lazyMessage().toString())
    }

}