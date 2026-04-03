package io.github.kotlinx.jdbc.internal.argument

import io.github.kotlinx.jdbc.spi.SqlArgument
import io.github.kotlinx.jdbc.spi.SqlArgumentFactory
import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZonedDateTime

class JavaTimeArgumentFactory : SqlArgumentFactory {
    override fun create(value: Any): SqlArgument? {
        return when (value) {
            is Instant -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, Timestamp.from(value)) }
            is LocalDate -> SqlArgument { position, pstmt -> pstmt.setDate(position, Date.valueOf(value)) }
            is LocalTime -> SqlArgument { position, pstmt -> pstmt.setTime(position, Time.valueOf(value)) }
            is LocalDateTime -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, Timestamp.valueOf(value)) }
            is OffsetDateTime -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, Timestamp.from(value.toInstant()))}
            is ZonedDateTime -> SqlArgument { position, pstmt -> pstmt.setTimestamp(position, Timestamp.from(value.toInstant())) }
            else -> null
        }
    }
}