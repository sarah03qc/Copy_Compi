package edu.ic5701.tokens

data class Token (
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val column: Int
) {
    override fun toString(): String =
        "Token(tipo=$type, lexema=\"$lexeme\", línea=$line, columna=$column)"
}