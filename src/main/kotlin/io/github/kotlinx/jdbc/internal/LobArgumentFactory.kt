package io.github.kotlinx.jdbc.internal

import io.github.kotlinx.jdbc.spi.sql.argument.SqlArgument
import io.github.kotlinx.jdbc.spi.sql.argument.factory.SqlArgumentFactory
import java.sql.Blob
import java.sql.Clob

/**
 * [SqlArgumentFactory] implementation for [Blob] & [Clob] types
 */
class LobArgumentFactory: SqlArgumentFactory {
    override fun build(value: Any): SqlArgument? {
        return when (value) {
            is Clob -> SqlArgument { position, pstmt -> pstmt.setClob(position, value) }
            is Blob -> SqlArgument { position, pstmt -> pstmt.setBlob(position, value) }
            else -> null
        }
    }
}