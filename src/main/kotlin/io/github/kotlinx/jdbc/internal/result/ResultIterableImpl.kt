package io.github.kotlinx.jdbc.internal.result

import io.github.kotlinx.jdbc.exception.EmptyResultException
import io.github.kotlinx.jdbc.result.ResultIterable
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

    override fun first(): T = resultSetProducer.withResultSet {
        if (!it.next()) throw EmptyResultException("Expected at least one row but found none")
        mapper.map(it)
    }

    override fun firstOrNull(): T? = resultSetProducer.withResultSet {
        if (it.next()) mapper.map(it) else null
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

}