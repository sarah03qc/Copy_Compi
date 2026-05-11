package edu.ic5701.parser

import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType

/**
 * encapsula el cursor de lectura sobre la lista de tokens producida por el scanner.
 * centraliza las operaciones de navegacion para que el parser no dependa
 * directamente de indices ni de la lista de tokens.
 */
internal class TokenStream(private val tokens: List<Token>) {

    private var current: Int = 0

    /**
     * retorna el token actual sin consumirlo.
     */
    fun peek(): Token = tokens[current]

    /**
     * retorna el token en la posicion anterior a la actual.
     * util para mensajes de error que referencian el ultimo token consumido.
     */
    fun previous(): Token = tokens[maxOf(0, current - 1)]

    /**
     * retorna true si el token actual es del tipo indicado.
     */
    fun check(type: TokenType): Boolean = peek().type == type

    /**
     * retorna true si el token actual es EOF.
     */
    fun isAtEnd(): Boolean = check(TokenType.EOF)

    fun currentIndex(): Int = current

    /**
     * consume el token actual, avanza al siguiente y retorna el token consumido.
     */
    fun advance(): Token {
        val token = tokens[current]
        if (!isAtEnd()) current++
        return token
    }

    /**
     * retorna true si el token actual pertenece al conjunto de tipos indicado.
     */
    fun checkAny(vararg types: TokenType): Boolean = peek().type in types
}