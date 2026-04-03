package io.github.kotlinx.jdbc.spi

import java.sql.PreparedStatement


fun interface SqlArgument {
    fun apply(position: Int, pstmt: PreparedStatement)
}