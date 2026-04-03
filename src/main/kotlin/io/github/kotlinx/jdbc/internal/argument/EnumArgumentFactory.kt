package io.github.kotlinx.jdbc.internal.argument

import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.spi.SqlArgumentFactory

/**
 * [SqlArgumentFactory] for Enum. Enum name is used as the value
 */
class EnumArgumentFactory: SqlArgumentFactory {
    override fun create(value: Any): SqlArgument? {
        return when (value) {
            is Enum<*> -> SqlArgument { position, pstmt -> pstmt.setString(position, value.name) }
            else -> null
        }
    }
}