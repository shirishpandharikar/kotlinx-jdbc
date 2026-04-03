package io.github.kotlinx.test

enum class UserStatus {
    ACTIVE,
    INACTIVE
}

data class User(
    val id: Long,
    val name: String,
    val status: UserStatus,
    val age: Int
)