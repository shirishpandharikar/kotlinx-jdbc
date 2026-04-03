package io.github.kotlinx.jdbc.internal.argument

import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.spi.SqlArgumentFactory
import java.sql.Blob
import java.sql.Clob

/**
 * [SqlArgumentFactory] implementation for [Blob] & [Clob] types
 */
internal class LobArgumentFactory: SqlArgumentFactory {
    override fun create(value: Any): SqlArgument? {
        return when (value) {
            is Clob -> SqlArgument { position, pstmt -> pstmt.setClob(position, value) }
            is Blob -> SqlArgument { position, pstmt -> pstmt.setBlob(position, value) }
            else -> null
        }
    }
}