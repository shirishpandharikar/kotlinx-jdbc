package io.github.kotlinx.jdbc.internal

import io.github.kotlinx.jdbc.spi.sql.argument.SqlArgument
import io.github.kotlinx.jdbc.spi.sql.argument.factory.SqlArgumentFactory

class EssentialArgumentFactory: SqlArgumentFactory {
    override fun build(value: Any): SqlArgument? {
        return when (value) {
            is Boolean -> SqlArgument { position, pstmt -> pstmt.setBoolean(position, value) }
            is Byte -> SqlArgument { position, pstmt -> pstmt.setByte(position, value) }
            is Char -> SqlArgument { position, pstmt -> pstmt.setString(position, value.toString()) }
            is Short -> SqlArgument { position, pstmt -> pstmt.setShort(position, value) }
            is Int -> SqlArgument { position, pstmt -> pstmt.setInt(position, value) }
            is String -> SqlArgument { position, pstmt -> pstmt.setString(position, value) }
            is Long -> SqlArgument { position, pstmt -> pstmt.setLong(position, value) }
            is Double -> SqlArgument { position, pstmt -> pstmt.setDouble(position, value) }
            else -> null
        }
    }
}