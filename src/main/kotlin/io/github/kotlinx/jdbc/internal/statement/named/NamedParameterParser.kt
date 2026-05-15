package io.github.kotlinx.jdbc.internal.statement.named

object NamedParameterParser {

    fun parse(sql: String): ParsedSql {
        require(sql.isNotEmpty()) { "SQL must not be empty." }
        val ctx = ParsingContext(sql)

        while (!ctx.eof()) {
            when {
                handleQuotedLiteral(ctx) -> Unit
                handleQuotedIdentifier(ctx) -> Unit
                handleLineComment(ctx) -> Unit
                handleBlockComment(ctx) -> Unit
                handleDollarQuoted(ctx) -> Unit
                handlePositionalPlaceholder(ctx) -> Unit
                handleNamedParameter(ctx) -> Unit
                else -> ctx.copyCurrentAndAdvance()
            }
        }
        return toParsedSql(ctx)
    }

    private fun toParsedSql(ctx: ParsingContext): ParsedSql {
        val indexMap = buildIndexMap(ctx.namesByOccurrence)
        return ParsedSql(
            originalSql = ctx.sql,
            jdbcSql = ctx.out.toString(),
            parameterNamesByOccurrence = ctx.namesByOccurrence.toList(),
            parameterIndexesByName = indexMap,
            namedParameterCount = ctx.namesByOccurrence.toSet().size,
            unnamedParameterCount = ctx.unnamedCount,
            totalParameterCount = ctx.namesByOccurrence.size + ctx.unnamedCount
        )
    }

    private fun buildIndexMap(namesByOccurrence: List<String>): Map<String, IntArray> {
        if (namesByOccurrence.isEmpty()) return emptyMap()

        val tmp = LinkedHashMap<String, MutableList<Int>>()
        for (i in namesByOccurrence.indices) {
            val oneBased = i + 1
            tmp.computeIfAbsent(namesByOccurrence[i]) { ArrayList(2) }.add(oneBased)
        }

        val out = LinkedHashMap<String, IntArray>(tmp.size)
        for ((name, indexes) in tmp) {
            out[name] = indexes.toIntArray()
        }
        return out
    }

    // -----------------------------
    // Main handlers (single purpose)
    // -----------------------------

    private fun handleQuotedLiteral(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '\'') return false
        copySingleQuoted(ctx)
        return true
    }

    private fun handleQuotedIdentifier(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '"') return false
        copyDoubleQuotedIdentifier(ctx)
        return true
    }

    private fun handleLineComment(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '-' || !ctx.hasNext() || ctx.nextChar() != '-') return false
        copyLineComment(ctx)
        return true
    }

    private fun handleBlockComment(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '/' || !ctx.hasNext() || ctx.nextChar() != '*') return false
        copyBlockComment(ctx)
        return true
    }

    private fun handleDollarQuoted(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '$') return false
        return copyDollarQuotedIfPresent(ctx)
    }

    private fun handlePositionalPlaceholder(ctx: ParsingContext): Boolean {
        if (ctx.ch() != '?') return false

        // Preserve Postgres operators: ?| ?& ??
        if (ctx.hasNext()) {
            val n = ctx.nextChar()
            if (n == '|' || n == '&' || n == '?') {
                ctx.append('?')
                ctx.advance()
                return true
            }
        }

        ctx.unnamedCount++
        ctx.append('?')
        ctx.advance()
        return true
    }

    private fun handleNamedParameter(ctx: ParsingContext): Boolean {
        if (ctx.ch() != ':') return false

        // Postgres cast (::)
        if (ctx.hasNext() && ctx.nextChar() == ':') {
            ctx.append("::")
            ctx.advance(2)
            return true
        }

        // Braced form :{name}
        if (ctx.hasNext() && ctx.nextChar() == '{') {
            return consumeBracedNamedParameter(ctx)
        }

        // Regular form :name
        return consumeRegularNamedParameter(ctx)
    }

    // -----------------------------
    // Named parameter consumers
    // -----------------------------

    private fun consumeBracedNamedParameter(ctx: ParsingContext): Boolean {
        val start = ctx.index + 2 // after :{
        val end = findClosingBrace(ctx.sql, start)
        if (end < 0) {
            // not a valid braced parameter; treat ':' literally
            ctx.append(':')
            ctx.advance()
            return true
        }

        val name = ctx.sql.substring(start, end)
        validateParamName(name, ctx.sql, ctx.index)

        ctx.namesByOccurrence.add(name)
        ctx.append('?')
        ctx.index = end + 1
        return true
    }

    private fun consumeRegularNamedParameter(ctx: ParsingContext): Boolean {
        if (!ctx.hasNext() || !isParamNameStart(ctx.nextChar())) {
            // Not a named parameter token.
            ctx.append(':')
            ctx.advance()
            return true
        }

        var end = ctx.index + 2
        while (end < ctx.sql.length && isParamNamePart(ctx.sql[end])) {
            end++
        }

        val name = ctx.sql.substring(ctx.index + 1, end)
        ctx.namesByOccurrence.add(name)
        ctx.append('?')
        ctx.index = end
        return true
    }

    // -----------------------------
    // Copy helpers
    // -----------------------------

    private fun copySingleQuoted(ctx: ParsingContext) {
        ctx.append('\'')
        ctx.advance()

        while (!ctx.eof()) {
            val c = ctx.ch()
            ctx.append(c)
            ctx.advance()

            if (c == '\'') {
                // escaped '' in SQL literal
                if (!ctx.eof() && ctx.ch() == '\'') {
                    ctx.append('\'')
                    ctx.advance()
                    continue
                }
                return
            }

            // dialects that allow backslash escapes
            if (c == '\\' && !ctx.eof()) {
                ctx.append(ctx.ch())
                ctx.advance()
            }
        }
    }

    private fun copyDoubleQuotedIdentifier(ctx: ParsingContext) {
        ctx.append('"')
        ctx.advance()

        while (!ctx.eof()) {
            val c = ctx.ch()
            ctx.append(c)
            ctx.advance()

            if (c == '"') {
                // escaped ""
                if (!ctx.eof() && ctx.ch() == '"') {
                    ctx.append('"')
                    ctx.advance()
                    continue
                }
                return
            }
        }
    }

    private fun copyLineComment(ctx: ParsingContext) {
        ctx.append("--")
        ctx.advance(2)

        while (!ctx.eof()) {
            val c = ctx.ch()
            ctx.append(c)
            ctx.advance()
            if (c == '\n') return
        }
    }

    private fun copyBlockComment(ctx: ParsingContext) {
        ctx.append("/*")
        ctx.advance(2)

        while (!ctx.eof()) {
            val c = ctx.ch()
            ctx.append(c)

            if (c == '*' && ctx.hasNext() && ctx.nextChar() == '/') {
                ctx.append('/')
                ctx.advance(2)
                return
            }

            ctx.advance()
        }
    }

    private fun copyDollarQuotedIfPresent(ctx: ParsingContext): Boolean {
        val start = ctx.index
        var i = start + 1

        while (i < ctx.sql.length && (ctx.sql[i] == '_' || ctx.sql[i].isLetterOrDigit())) {
            i++
        }

        if (i >= ctx.sql.length || ctx.sql[i] != '$') {
            return false
        }

        val delimiter = ctx.sql.substring(start, i + 1)
        ctx.append(delimiter)
        i++

        while (i < ctx.sql.length) {
            if (i + delimiter.length <= ctx.sql.length &&
                ctx.sql.regionMatches(i, delimiter, 0, delimiter.length)
            ) {
                ctx.append(delimiter)
                ctx.index = i + delimiter.length
                return true
            }
            ctx.append(ctx.sql[i])
            i++
        }

        // Unclosed dollar quote: copy to end
        ctx.index = i
        return true
    }

    // -----------------------------
    // Small lexical helpers
    // -----------------------------

    private fun findClosingBrace(sql: String, from: Int): Int {
        var i = from
        while (i < sql.length) {
            if (sql[i] == '}') return i
            i++
        }
        return -1
    }

    private fun validateParamName(name: String, sql: String, pos: Int) {
        require(name.isNotBlank()) {
            "Named parameter must not be blank at position $pos in SQL: [$sql]"
        }
        require(isParamNameStart(name[0])) {
            "Named parameter '$name' must start with letter or '_' at position $pos in SQL: [$sql]"
        }
        for (c in name) {
            require(isParamNamePart(c)) {
                "Named parameter '$name' contains illegal character '$c' at position $pos in SQL: [$sql]"
            }
        }
    }

    private fun isParamNameStart(c: Char): Boolean = c == '_' || c.isLetter()
    private fun isParamNamePart(c: Char): Boolean = c == '_' || c.isLetterOrDigit()

    private class ParsingContext(val sql: String) {
        var index: Int = 0
        val out = StringBuilder()
        val namesByOccurrence = ArrayList<String>()
        var unnamedCount: Int = 0

        fun eof(): Boolean = index >= sql.length
        fun ch(): Char = sql[index]
        fun hasNext(): Boolean = index + 1 < sql.length
        fun nextChar(): Char = sql[index + 1]

        fun advance(step: Int = 1) {
            index += step
        }

        fun append(c: Char) {
            out.append(c)
        }

        fun append(text: String) {
            out.append(text)
        }

        fun copyCurrentAndAdvance() {
            out.append(sql[index])
            index++
        }
    }

}