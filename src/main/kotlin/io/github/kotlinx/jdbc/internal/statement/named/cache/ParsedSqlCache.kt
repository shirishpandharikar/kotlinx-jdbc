package io.github.kotlinx.jdbc.internal.statement.named.cache

import io.github.kotlinx.jdbc.internal.statement.named.NamedParameterParser
import io.github.kotlinx.jdbc.internal.statement.named.ParsedSql

/**
 * A simple, thread-safe, bounded cache for [ParsedSql] keyed by the raw SQL string.
 *
 * Parsing named SQL is a pure function of the input string, so results are safely
 * cacheable and reusable across handles and threads. The cache is bounded with an
 * LRU eviction policy to avoid unbounded growth when SQL is built dynamically.
 */
internal class ParsedSqlCache(
    private val maxSize: Int = DEFAULT_MAX_SIZE,
    private val parser: (String) -> ParsedSql = NamedParameterParser::parse
) {

    private val cache =
        object : LinkedHashMap<String, ParsedSql>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<String, ParsedSql>): Boolean =
            size > maxSize
    }

    /**
     * Returns the [ParsedSql] for [sql], parsing and caching it on first use.
     */
    fun get(sql: String): ParsedSql = synchronized(cache) {
        cache[sql] ?: parser(sql).also { cache[sql] = it }
    }

    companion object {
        /**
         * Maximum number of cache entries after which the cache removes the eldest or the least recently used
         */
        const val DEFAULT_MAX_SIZE = 256
    }
}