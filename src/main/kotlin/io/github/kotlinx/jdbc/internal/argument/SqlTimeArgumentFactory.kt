package io.github.kotlinx.jdbc.internal.argument

import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.spi.SqlArgumentFactory
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp

class SqlTimeArgumentFactory: SqlArgumentFactory {
    override fun create(value: Any): SqlArgument? {
        return when (value) {
            is Timestamp -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, value) }
            is Date -> SqlArgument { position, pstmt -> pstmt.setDate(position, value) }
            is Time -> SqlArgument { position, pstmt -> pstmt.setTime(position, value) }
            is java.util.Date -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, Timestamp(value.time)) }
            else -> null
        }
    }
}