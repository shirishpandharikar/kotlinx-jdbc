package io.github.kotlinx.jdbc.exception

class EmptyResultException(message: String) : NoSuchElementException(message)
class TooManyRowsException(message: String) : IllegalStateException(message)