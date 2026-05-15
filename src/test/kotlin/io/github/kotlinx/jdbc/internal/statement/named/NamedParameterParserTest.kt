package io.github.kotlinx.jdbc.internal.statement.named

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class NamedParameterParserTest: ShouldSpec() {

    init {

        should("parse SQL with named parameters") {
            val parsed = NamedParameterParser.parse(
                "select * from users where id = :id and status = :status"
            )

            parsed.jdbcSql shouldBe "select * from users where id = ? and status = ?"
            parsed.parameterNamesByOccurrence shouldContainExactly listOf("id", "status")
            parsed.namedParameterCount shouldBe 2
            parsed.unnamedParameterCount shouldBe 0
            parsed.totalParameterCount shouldBe 2
            parsed.parameterIndexesByName["id"]?.toList() shouldBe listOf(1)
            parsed.parameterIndexesByName["status"]?.toList() shouldBe listOf(2)
        }

        should("support repeated named parameters and map all indexes") {
            val parsed = NamedParameterParser.parse(
                "select * from t where id = :id or parent_id = :id or owner_id = :owner"
            )

            parsed.jdbcSql shouldBe "select * from t where id = ? or parent_id = ? or owner_id = ?"
            parsed.parameterNamesByOccurrence shouldContainExactly listOf("id", "id", "owner")
            parsed.namedParameterCount shouldBe 2
            parsed.totalParameterCount shouldBe 3
            parsed.parameterIndexesByName["id"]?.toList() shouldBe listOf(1, 2)
            parsed.parameterIndexesByName["owner"]?.toList() shouldBe listOf(3)
        }

        should("parse SQL with only positional parameters") {
            val parsed = NamedParameterParser.parse(
                "select * from users where id = ? and status = ?"
            )

            parsed.jdbcSql shouldBe "select * from users where id = ? and status = ?"
            parsed.parameterNamesByOccurrence shouldBe emptyList()
            parsed.namedParameterCount shouldBe 0
            parsed.unnamedParameterCount shouldBe 2
            parsed.totalParameterCount shouldBe 2
        }

        should("parse SQL with special positional parameters when using PostgreSQL") {
            val parsed = NamedParameterParser.parse(
                "select * from users where id = :id::uuid and status = :status"
            )
            parsed.jdbcSql shouldBe "select * from users where id = ?::uuid and status = ?"
            parsed.parameterNamesByOccurrence shouldContainExactly listOf("id", "status")
            parsed.namedParameterCount shouldBe 2
            parsed.unnamedParameterCount shouldBe 0
            parsed.totalParameterCount shouldBe 2
            parsed.parameterIndexesByName["id"]?.toList() shouldBe listOf(1)
            parsed.parameterIndexesByName["status"]?.toList() shouldBe listOf(2)
        }
    }

}