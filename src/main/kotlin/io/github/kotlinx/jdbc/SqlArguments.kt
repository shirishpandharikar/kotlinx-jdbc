package io.github.kotlinx.jdbc

import io.github.kotlinx.jdbc.internal.argument.LoggableSqlArgument
import io.github.kotlinx.jdbc.spi.SqlArgument
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.sql.Types.BLOB
import java.sql.Types.CLOB
import java.sql.Types.NCLOB

/**
 * Static factory for creating commonly used [SqlArgument] instances.
 */
object SqlArguments {

    fun ofNull(sqlType: Int): SqlArgument {
        return LoggableSqlArgument(null, { "NULL" }) { position, pstmt, _ -> pstmt.setNull(position, sqlType) }
    }

    fun ofCharacterSequence(value: CharSequence, sqlType: Int): SqlArgument {
        return when(sqlType) {
            CLOB -> LoggableSqlArgument(value, { "[CLOB of length ${it.length}]" }) { index, stmt, v -> stmt.setClob(index, StringReader(v.toString()),  v.length.toLong()) }
            NCLOB -> LoggableSqlArgument(value, { "[NCLOB of length ${it.length}]" }) { index, stmt, v -> stmt.setNClob(index, StringReader(v.toString()),  v.length.toLong()) }
            else -> LoggableSqlArgument(value, { "'$it'" }) { index, stmt, v -> stmt.setString(index, v.toString()) }
        }
    }

    fun ofByteArray(value: ByteArray, sqlType: Int): SqlArgument {
        return when (sqlType) {
            BLOB -> LoggableSqlArgument(value, { "Byte array(bytes=${it.size}, sqlType=$BLOB)" }) { index, stmt, v -> stmt.setBlob(index, ByteArrayInputStream(v), v.size.toLong()) }
            else -> LoggableSqlArgument(value, { "Byte array(bytes=${it.size})" }) { index, stmt, v -> stmt.setBytes(index, v) }
        }
    }

    fun ofAsciiStream(value: InputStream, length: Long): SqlArgument {
        return LoggableSqlArgument(value, { "[ASCII stream(length=$length)]" }) { index, stmt, v -> stmt.setAsciiStream(index, v, length) }
    }

    fun ofBinaryInputStream(value: InputStream, sqlType: Int, length: Long): SqlArgument {
        return when(sqlType) {
            BLOB -> LoggableSqlArgument(value, { "[Binary stream(length=$length, sqlType=$BLOB})]" }) { index, stmt, v -> stmt.setBlob(index, v, length) }
            else -> LoggableSqlArgument(value, { "[Binary stream(length=$length)]" }) { index, stmt, v -> stmt.setBinaryStream(index, v, length) }
        }
    }

    fun ofReader(value: Reader, sqlType: Int, length: Long): SqlArgument {
        return when(sqlType) {
            CLOB -> LoggableSqlArgument(value, { "[Reader(length=$length, sqlType=$CLOB)]" }) { index, stmt, v -> stmt.setClob(index, v, length) }
            NCLOB -> LoggableSqlArgument(value, { "[Reader(length=$length, sqlType=$NCLOB)]" }) { index, stmt, v -> stmt.setNClob(index, v, length) }
            else -> LoggableSqlArgument(value, { "[Reader(length=$length)]" }) { index, stmt, v -> stmt.setCharacterStream(index, v, length) }
        }
    }

}