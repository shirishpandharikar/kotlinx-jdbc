package io.github.kotlinx.jdbc.argument

import io.github.kotlinx.jdbc.BaseSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.TimeZone

class JavaTimeArgumentFactorySpec : BaseSpec() {

    private companion object {
        private val UTC_CAL = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        private const val SELECT = "SELECT * FROM time_entries WHERE id = ?"
    }

    init {

        should("store and retrieve Instant at the correct UTC instant") {
            val instant = Instant.now().truncatedTo(ChronoUnit.MICROS)

            val id = insertSingle(instant)

            val stored = dbi.withHandle {
                query(SELECT).bind(1, id).map { it.getTimestamp("instant_col", UTC_CAL).toInstant() }.first()
            }
            stored shouldBe instant
        }

        should("store and retrieve LocalDate without any date shift") {
            val date = LocalDate.of(2024, 6, 15)

            val id = insertSingle(date)

            val stored = dbi.withHandle {
                query(SELECT).bind(1, id).map { LocalDate.parse(it.getString("local_date_col")) }.first()
            }
            stored shouldBe date
        }

        should("store and retrieve LocalTime without any time shift") {
            val time = LocalTime.of(14, 30, 45)

            val id = insertSingle(time)

            val stored = dbi.withHandle {
                query(SELECT).bind(1, id).map { it.getTime("local_time_col").toLocalTime() }.first()
            }
            stored shouldBe time
        }

        should("store and retrieve LocalDateTime as wall-clock time without any shift") {
            val dateTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45)

            val id = insertSingle(dateTime)

            val stored = dbi.withHandle {
                query(SELECT).bind(1, id).map { it.getTimestamp("local_datetime_col").toLocalDateTime() }.first()
            }
            stored shouldBe dateTime
        }

        should("store OffsetDateTime preserving the correct UTC instant regardless of offset") {
            // +05:30 offset — a common source of bugs when offset is silently stripped
            val offsetDt = OffsetDateTime.of(2024, 6, 15, 16, 30, 0, 0, ZoneOffset.of("+05:30"))
                .truncatedTo(ChronoUnit.MICROS)
            val expectedInstant = offsetDt.toInstant()

            val id = insertSingle(offsetDt)

            val storedInstant = dbi.withHandle {
                query(SELECT).bind(1, id).map { it.getTimestamp("offset_datetime_col", UTC_CAL).toInstant() }.first()
            }
            storedInstant shouldBe expectedInstant
        }

        should("store ZonedDateTime preserving the correct UTC instant regardless of zone") {
            val zonedDt = ZonedDateTime.of(2024, 6, 15, 16, 30, 0, 0, ZoneId.of("Asia/Kolkata"))
                .truncatedTo(ChronoUnit.MICROS)
            val expectedInstant = zonedDt.toInstant()

            val id = insertSingle(zonedDt)

            val storedInstant = dbi.withHandle {
                query(SELECT).bind(1, id).map { it.getTimestamp("zoned_datetime_col", UTC_CAL).toInstant() }.first()
            }
            storedInstant shouldBe expectedInstant
        }
    }

    /**
     * Inserts a single non-null value into its corresponding column,
     * leaving all other columns null. Returns the generated id.
     */
    private fun insertSingle(value: Any): Long {
        val col = when (value) {
            is Instant        -> "instant_col"
            is LocalDate      -> "local_date_col"
            is LocalTime      -> "local_time_col"
            is LocalDateTime  -> "local_datetime_col"
            is OffsetDateTime -> "offset_datetime_col"
            is ZonedDateTime  -> "zoned_datetime_col"
            else -> error("Unsupported type: ${value::class}")
        }
        return dbi.withHandle {
            update("INSERT INTO time_entries ($col) VALUES (?)").bind(1, value)
                .executeWithGeneratedKeys("id") { it.getLong("id") }.first()
        }
    }
}
