package interpreter

enum class TokenInfo {
    SEMICOLON, SKIP, READ, WRITE, ASSIGNMENT, IF, THEN, ELSE, FI, WHILE, DO, OD,
    OR, AND, NOT, OPENINGBRACKET, CLOSINGBRACKET, NEWLINE, AWEAKOP, ASTRONGOP, RELATION,
    BCONSTANT, NUMBER, VARNAME, WHITESPACE, COMMA, HELPTOKEN
}

data class Token(val value: String, val info: TokenInfo)

class Tokenizer(private val matcher: Matcher = Matcher()) {

    class Matcher(private val matchers: MutableList<Pair<Regex, TokenInfo>> = mutableListOf()) {
        fun add(map: Pair<String, TokenInfo>) = matchers.add("^(${map.first})".toRegex() to map.second)
        fun addRange(list: List<Pair<String, TokenInfo>>) = list.forEach { add(it) }
        fun matchFirst(text: String): Pair<String, TokenInfo> {
            matchers.forEach {
                val match = it.first.find(text)
                if (match != null) return match.value to it.second
            }
            throw Exception("None matches found, for token starting with '${text.substring(0..10)}'")
        }

    }

    init {
        matcher.addRange(
            listOf(
                ";" to TokenInfo.SEMICOLON,
                "," to TokenInfo.COMMA,
                "skip" to TokenInfo.SKIP,
                "read" to TokenInfo.READ,
                "write" to TokenInfo.WRITE,
                ":=" to TokenInfo.ASSIGNMENT,
                "if" to TokenInfo.IF,
                "then" to TokenInfo.THEN,
                "else" to TokenInfo.ELSE,
                "fi" to TokenInfo.FI,
                "while" to TokenInfo.WHILE,
                "do" to TokenInfo.DO,
                "od" to TokenInfo.OD,
                "or" to TokenInfo.OR,
                "and" to TokenInfo.AND,
                "not" to TokenInfo.NOT,
                "\\(" to TokenInfo.OPENINGBRACKET,
                "\\)" to TokenInfo.CLOSINGBRACKET,
                "\\r?\\n" to TokenInfo.NEWLINE,
                "[+-]" to TokenInfo.AWEAKOP,
                "[*/]" to TokenInfo.ASTRONGOP,
                "<>|=<|>=|=|<|>" to TokenInfo.RELATION,
                "true|false" to TokenInfo.BCONSTANT,
                "[1-9][0-9]*|0" to TokenInfo.NUMBER,
                "([a-z]|[A-Z]|_)([a-z]|[A-Z]|[0-9]|_)*" to TokenInfo.VARNAME,
                "[ \\t\\r\\n]" to TokenInfo.WHITESPACE
            )
        )
    }

    fun tokenize(string: String): List<Token> {
        val resultTokens = mutableListOf<Token>()
        var curString = string

        while (curString.isNotEmpty()) {
            val match = matcher.matchFirst(curString)
            curString = curString.replaceFirst(match.first, "")
            if (match.second != TokenInfo.WHITESPACE) {
                resultTokens.add(Token(match.first, match.second))
            }
        }

        return resultTokens
    }
}