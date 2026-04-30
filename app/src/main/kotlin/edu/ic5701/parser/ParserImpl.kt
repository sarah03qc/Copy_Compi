package edu.ic5701.parser

import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType

/**
 * parser de descenso recursivo para el lenguaje CR++.
 *
 * recibe la lista de tokens producida por el scanner y verifica que la secuencia
 * sea sintacticamente valida segun la gramatica LL(1) documentada en
 * docs/03 GRAMATICA.md. cada no-terminal de la gramatica tiene su propio metodo
 * privado, siguiendo la estructura de descenso recursivo vista en clase.
 *
 * las responsabilidades de navegacion de tokens y reporte de errores se delegan
 * a TokenStream y ErrorReporter respectivamente, manteniendo este archivo
 * enfocado exclusivamente en la logica de parsing.
 */
class ParserImpl(tokens: List<Token>) : Parser {

    private val stream = TokenStream(tokens)
    private val reporter = ErrorReporter(stream)

    override val errors: List<String> get() = reporter.errors

    // -----------------------------------------------------------
    // punto de entrada
    // -----------------------------------------------------------

    override fun parse(): Boolean {
        parsePrograma()
        return errors.isEmpty()
    }

    // -----------------------------------------------------------
    // utilitarios locales
    // -----------------------------------------------------------

    private fun peek(): Token = stream.peek()

    private fun check(type: TokenType): Boolean = stream.check(type)

    private fun advance(): Token = stream.advance()

    /**
     * si el token actual es del tipo esperado lo consume y lo retorna.
     * de lo contrario registra el error y lanza ParseException.
     */
    private fun expect(type: TokenType): Token {
        if (check(type)) return advance()
        throw reporter.expectedError(type)
    }

    private fun reportError(message: String): ParseException =
        reporter.reportError(message)

    private fun synchronize() = reporter.synchronize()

    // -----------------------------------------------------------
    // producciones 1-3: programa
    // -----------------------------------------------------------

    private fun parsePrograma() {
        try {
            parseDeclFunc()
        } catch (e: ParseException) {
            synchronize()
        }
        parseProgramaPrima()
    }

    private fun parseProgramaPrima() {
        while (check(TokenType.FUNCION)) {
            try {
                parseDeclFunc()
            } catch (e: ParseException) {
                synchronize()
            }
        }
        try {
            expect(TokenType.EOF)
        } catch (e: ParseException) {
            // el error ya fue registrado en reporter, solo evitamos que escape
        }
    }

    // -----------------------------------------------------------
    // produccion 4: declaracion de funcion
    // -----------------------------------------------------------

    private fun parseDeclFunc() {
        expect(TokenType.FUNCION)
        parseTipo()
        expect(TokenType.IDENT)
        expect(TokenType.PAREN_ABIERTO)
        parseParams()
        expect(TokenType.PAREN_CERRADO)
        parseBloque()
        expect(TokenType.DOS_PUNTOS)
        parseTipo()
    }

    // -----------------------------------------------------------
    // producciones 5-9: tipos
    // -----------------------------------------------------------

    private fun parseTipo() {
        when {
            check(TokenType.ENTERO)   -> advance()
            check(TokenType.BOOLEANO) -> advance()
            check(TokenType.CADENA)   -> advance()
            check(TokenType.VACIO)    -> advance()
            check(TokenType.ARREGLO)  -> {
                advance()
                expect(TokenType.PAREN_ABIERTO)
                parseTipo()
                expect(TokenType.PAREN_CERRADO)
            }
            else -> throw reportError(
                "se esperaba un tipo (colones, diay, labia, ni_papa, fila_india) " +
                "pero se encontro '${peek().lexeme}' en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    // -----------------------------------------------------------
    // producciones 10-14: parametros
    // -----------------------------------------------------------

    private fun parseParams() {
        if (isTipoToken()) {
            parseParam()
            parseParamsPrima()
        }
    }

    private fun parseParamsPrima() {
        while (check(TokenType.SEPARAR)) {
            advance()
            parseParam()
        }
    }

    private fun parseParam() {
        parseTipo()
        expect(TokenType.IDENT)
    }

    // -----------------------------------------------------------
    // producciones 15-18: bloque y sentencias
    // -----------------------------------------------------------

    private fun parseBloque() {
        expect(TokenType.CORCHETE_ABIERTO)
        parseSentencias()
        expect(TokenType.CORCHETE_CERRADO)
    }

    private fun parseSentencias() {
        while (isSentenciaToken()) {
            try {
                parseSentencia()
            } catch (e: ParseException) {
                synchronize()
            }
        }
    }

    // -----------------------------------------------------------
    // producciones 19-27: dispatcher de sentencias
    // -----------------------------------------------------------

    private fun parseSentencia() {
        when (peek().type) {
            TokenType.VARIABLE -> parseDeclVar()
            TokenType.CONSTANTE -> parseDeclConst()
            TokenType.RETORNAR -> parseSentRetorno()
            TokenType.CONDICIONAL -> parseSentCond()
            TokenType.CICLO_INDEF -> parseSentCiclo()
            TokenType.IMPRIMIR -> parseSentPrint()
            TokenType.BREAK -> parseSentBreak()
            TokenType.CONTINUE -> parseSentContinue()
            TokenType.IDENT -> parseSentExpr()
            else -> throw reportError(
                "token inesperado '${peek().lexeme}' al inicio de sentencia " +
                "en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    // -----------------------------------------------------------
    // producciones 28-29: declaraciones de variable y constante
    // -----------------------------------------------------------

    private fun parseDeclVar() {
        expect(TokenType.VARIABLE)
        parseTipo()
        expect(TokenType.IDENT)
        expect(TokenType.OP_ASIG)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    private fun parseDeclConst() {
        expect(TokenType.CONSTANTE)
        parseTipo()
        expect(TokenType.IDENT)
        expect(TokenType.OP_ASIG)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    // -----------------------------------------------------------
    // producciones 30-36: sentencias de control
    // -----------------------------------------------------------

    private fun parseSentRetorno() {
        expect(TokenType.RETORNAR)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    private fun parseSentCond() {
        expect(TokenType.CONDICIONAL)
        expect(TokenType.PAREN_ABIERTO)
        parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        parseBloque()
        parseSinoOpc()
    }

    private fun parseSinoOpc() {
        if (check(TokenType.SINO)) {
            advance()
            parseBloque()
        }
    }

    private fun parseSentCiclo() {
        expect(TokenType.CICLO_INDEF)
        expect(TokenType.PAREN_ABIERTO)
        parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        parseBloque()
    }

    private fun parseSentBreak() {
        expect(TokenType.BREAK)
        expect(TokenType.FIN_SENTENCIA)
    }

    private fun parseSentContinue() {
        expect(TokenType.CONTINUE)
        expect(TokenType.FIN_SENTENCIA)
    }

    // -----------------------------------------------------------
    // produccion 37: sentencia de impresion
    // -----------------------------------------------------------

    private fun parseSentPrint() {
        expect(TokenType.IMPRIMIR)
        expect(TokenType.PAREN_ABIERTO)
        parseArgs()
        expect(TokenType.PAREN_CERRADO)
        expect(TokenType.FIN_SENTENCIA)
    }

    // -----------------------------------------------------------
    // producciones 38-45: sentencias de expresion
    // -----------------------------------------------------------

    private fun parseSentExpr() {
        expect(TokenType.IDENT)
        parseSentIdentCola()
    }

    private fun parseSentIdentCola() {
        when (peek().type) {
            TokenType.OP_ASIG -> {
                advance()
                parseExpresion()
                expect(TokenType.FIN_SENTENCIA)
            }
            TokenType.PAREN_ABIERTO -> {
                advance()
                parseArgs()
                expect(TokenType.PAREN_CERRADO)
                expect(TokenType.FIN_SENTENCIA)
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseSentIndexCola()
            }
            TokenType.OP_INC -> {
                advance()
                expect(TokenType.FIN_SENTENCIA)
            }
            TokenType.OP_DEC -> {
                advance()
                expect(TokenType.FIN_SENTENCIA)
            }
            else -> throw reportError(
                "se esperaba '=', '(', '[', '++' o '--' despues del identificador " +
                "'${stream.previous().lexeme}' en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    private fun parseSentIndexCola() {
        when (peek().type) {
            TokenType.OP_ASIG -> {
                advance()
                parseExpresion()
                expect(TokenType.FIN_SENTENCIA)
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseSentIndexCola()
            }
            else -> throw reportError(
                "se esperaba '=' o '[' despues del acceso al arreglo " +
                "en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    // -----------------------------------------------------------
    // producciones 46-49: argumentos
    // -----------------------------------------------------------

    private fun parseArgs() {
        if (isExpresionToken()) {
            parseExpresion()
            parseArgsPrima()
        }
    }

    private fun parseArgsPrima() {
        while (check(TokenType.SEPARAR)) {
            advance()
            parseExpresion()
        }
    }

    // -----------------------------------------------------------
    // producciones 50-96: expresiones
    // -----------------------------------------------------------

    private fun parseExpresion() {
        parseOrExpr()
    }

    private fun parseOrExpr() {
        parseAndExpr()
        while (check(TokenType.OP_O)) {
            advance()
            parseAndExpr()
        }
    }

    private fun parseAndExpr() {
        parseEqualExpr()
        while (check(TokenType.OP_Y)) {
            advance()
            parseEqualExpr()
        }
    }

    private fun parseEqualExpr() {
        parseRelacExpr()
        while (stream.checkAny(TokenType.OP_IGU, TokenType.OP_DIFE)) {
            advance()
            parseRelacExpr()
        }
    }

    private fun parseRelacExpr() {
        parseAritExpr()
        while (stream.checkAny(
            TokenType.OP_MENOR,
            TokenType.OP_MENOR_IG,
            TokenType.OP_MAYOR,
            TokenType.OP_MAYOR_IG
        )) {
            advance()
            parseAritExpr()
        }
    }

    private fun parseAritExpr() {
        parseTermino()
        while (stream.checkAny(TokenType.OP_SUMA, TokenType.OP_RESTA)) {
            advance()
            parseTermino()
        }
    }

    private fun parseTermino() {
        parseFactor()
        while (stream.checkAny(TokenType.OP_MUL, TokenType.OP_DIV)) {
            advance()
            parseFactor()
        }
    }

    private fun parseFactor() {
        when {
            check(TokenType.OP_NO)    -> { advance(); parseFactor() }
            check(TokenType.OP_RESTA) -> { advance(); parseFactor() }
            else -> {
                parseFactorBase()
                parseFactorPrima()
            }
        }
    }

    private fun parseFactorBase() {
        when (peek().type) {
            TokenType.PAREN_ABIERTO -> {
                advance()
                parseExpresion()
                expect(TokenType.PAREN_CERRADO)
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                parseListaElementos()
                expect(TokenType.LLAVE_CERRADA)
            }
            TokenType.IDENT      -> advance()
            TokenType.LIT_ENTERO -> advance()
            TokenType.LIT_STRING -> advance()
            TokenType.VERDADERO  -> advance()
            TokenType.FALSO      -> advance()
            TokenType.VACIO      -> advance()
            TokenType.RECIBIR    -> {
                advance()
                expect(TokenType.PAREN_ABIERTO)
                expect(TokenType.PAREN_CERRADO)
            }
            else -> throw reportError(
                "se esperaba una expresion pero se encontro '${peek().lexeme}' " +
                "(${peek().type.name}) en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    private fun parseFactorPrima() {
        when (peek().type) {
            TokenType.OP_INC      -> advance()
            TokenType.OP_DEC      -> advance()
            TokenType.PAREN_ABIERTO -> {
                advance()
                parseArgs()
                expect(TokenType.PAREN_CERRADO)
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseFactorIndex()
            }
            else -> { /* epsilon */ }
        }
    }

    private fun parseFactorIndex() {
        if (check(TokenType.LLAVE_ABIERTA)) {
            advance()
            parseExpresion()
            expect(TokenType.LLAVE_CERRADA)
            parseFactorIndex()
        }
    }

    private fun parseListaElementos() {
        if (isExpresionToken()) {
            parseExpresion()
            while (check(TokenType.SEPARAR)) {
                advance()
                parseExpresion()
            }
        }
    }

    // -----------------------------------------------------------
    // utilidades de clasificacion de tokens
    // -----------------------------------------------------------

    private fun isTipoToken(): Boolean = stream.checkAny(
        TokenType.ENTERO,
        TokenType.BOOLEANO,
        TokenType.CADENA,
        TokenType.VACIO,
        TokenType.ARREGLO
    )

    private fun isSentenciaToken(): Boolean = stream.checkAny(
        TokenType.VARIABLE,
        TokenType.CONSTANTE,
        TokenType.RETORNAR,
        TokenType.CONDICIONAL,
        TokenType.CICLO_INDEF,
        TokenType.IMPRIMIR,
        TokenType.BREAK,
        TokenType.CONTINUE,
        TokenType.IDENT
    )

    private fun isExpresionToken(): Boolean = stream.checkAny(
        TokenType.OP_NO,
        TokenType.OP_RESTA,
        TokenType.PAREN_ABIERTO,
        TokenType.LLAVE_ABIERTA,
        TokenType.IDENT,
        TokenType.LIT_ENTERO,
        TokenType.LIT_STRING,
        TokenType.VERDADERO,
        TokenType.FALSO,
        TokenType.VACIO,
        TokenType.RECIBIR
    )
}