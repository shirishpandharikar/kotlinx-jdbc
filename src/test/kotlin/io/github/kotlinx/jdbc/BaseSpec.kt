package io.github.kotlinx.jdbc

import io.kotest.provided.ProjectConfig
import io.kotest.core.spec.style.ShouldSpec

abstract class BaseSpec : ShouldSpec() {
    protected val dbi = Kdbi(ProjectConfig.testDataSource)
}