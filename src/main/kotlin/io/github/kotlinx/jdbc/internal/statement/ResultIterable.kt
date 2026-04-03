package io.github.kotlinx.jdbc.internal.statement

interface ResultIterable<T> {
    fun list(): List<T>
    fun first(): T
    fun firstOrNull(): T?
    fun <K> associateBy(keySelector: (T) -> K): Map<K, T>
    fun <K, V> associateBy(keySelector: (T) -> K, valueTransform: (T) -> V): Map<K, V>
}