package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.testcontainers.JdbcDatabaseContainerProjectExtension
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

object ProjectConfig : AbstractProjectConfig() {

    private const val IMAGE_NAME = "postgres:18.3-alpine3.23"

    private val postgres = PostgreSQLContainer(IMAGE_NAME).apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        withInitScripts("schema.sql", "data.sql")
    }

    private val dbExtension = JdbcDatabaseContainerProjectExtension(postgres)

    val testDataSource: DataSource get() = dbExtension.mount {}

    override val extensions: List<Extension> = listOf(dbExtension)
}