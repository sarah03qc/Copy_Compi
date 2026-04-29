package edu.ic5701.parser

import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType

/**
 * parser de descenso recursivo para el lenguaje CR++
 *
 * recibe la lista de tokens producida por el scanner y verifica que la secuencia
 * sea sintacticamente valida segun la gramatica LL(1) documentada en
 * docs/03 GRAMATICA.md, cada no-terminal de la gramatica tiene su propio metodo
 * privado, siguiendo la estructura de descenso recursivo
 *
 * estrategia de recuperacion de errores: modo panico, cuando se detecta un token
 * inesperado se lanza ParseException, se reporta el error, y se descartan tokens
 * hasta encontrar un punto de sincronizacion (FIN_SENTENCIA o CORCHETE_CERRADO)
 * para intentar continuar el analisis
 */
class ParserImpl(private val tokens: List<Token>) : Parser {

    override val errors: MutableList<String> = mutableListOf()

    // conjunto de tokens que se usan como puntos de sincronizacion
    // en la recuperacion de errores por modo panico
    private val syncTokens = setOf(
        TokenType.FIN_SENTENCIA,
        TokenType.CORCHETE_CERRADO
    )

    private var current: Int = 0

    // punto de entrada

    override fun parse(): Boolean {
        parsePrograma()
        return errors.isEmpty()
    }
 
    // infraestructura del cursor

    /**
     * retorna el token actual sin consumirlo.
     */
    private fun peek(): Token = tokens[current]

    /**
     * retorna true si el token actual es del tipo indicado.
     */
    private fun check(type: TokenType): Boolean = peek().type == type

    /**
     * consume el token actual y avanza al siguiente.
     * retorna el token que fue consumido.
     */
    private fun advance(): Token {
        val token = tokens[current]
        if (!check(TokenType.EOF)) current++
        return token
    }

    /**
     * si el token actual es del tipo esperado lo consume y lo retorna.
     * de lo contrario reporta el error y lanza ParseException para
     * iniciar la recuperacion por modo panico.
     */
    private fun expect(type: TokenType): Token {
        if (check(type)) return advance()
        val found = peek()
        throw reportError(
            "se esperaba '${type.name}' pero se encontro '${found.lexeme}' " +
            "(${found.type.name}) en linea ${found.line}, columna ${found.column}"
        )
    }

    // reporte de errores y recuperacion

    /**
     * registra el mensaje de error en la lista publica y retorna una
     * ParseException lista para ser lanzada por el sitio que llama
     * separar el registro del lanzamiento permite que algunos sitios
     * solo registren sin interrumpir el flujo
     */
    private fun reportError(message: String): ParseException {
        val formatted = "Jaguarsh sintactico: $message"
        errors.add(formatted)
        System.err.println(formatted)
        return ParseException(formatted)
    }

    /**
     * recuperacion de errores por modo panico
     * descarta tokens hasta encontrar uno de los puntos de sincronizacion
     * definidos en syncTokens o hasta llegar al EOF
     */
    private fun synchronize() {
        while (!check(TokenType.EOF)) {
            if (peek().type in syncTokens) {
                advance()
                return
            }
            advance()
        }
    }

    // no-terminales 
    // producciones 1-3: programa

    /**
     * PROGRAMA -> DECL_FUNC PROGRAMA'
     * PROGRAMA' -> DECL_FUNC PROGRAMA' | epsilon
     *
     * el programa es una secuencia de una o mas declaraciones de funcion.
     * termina cuando se llega al EOF.
     */
    private fun parsePrograma() {
    try {
        parseDeclFunc()
    } catch (e: ParseException) {
        synchronize()
    }
    parseProgramaPrima()
    }

    /**
     * PROGRAMA' -> DECL_FUNC PROGRAMA' | epsilon
     *
     * mientras el token actual sea 'chunche' hay otra funcion que parsear.
     * si es EOF se deriva epsilon y se termina.
     */
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
            // ya se registro el error en reportError, solo evitamos que escape
        }
    }

    // produccion 4: declaracion de funcion

    /**
     * DECL_FUNC -> chunche TIPO IDENT ( PARAMS ) BLOQUE : TIPO
     *
     * una declaracion de funcion comienza con 'chunche', seguida del tipo
     * de retorno, el nombre, la lista de parametros entre parentesis,
     * el cuerpo entre llaves, y finalmente la anotacion de tipo de retorno
     * separada por dos puntos.
     */
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

    // produccion 5-9: tipos

    /**
     * TIPO -> colones | diay | labia | ni_papa | fila_india ( TIPO )
     *
     * los primeros cuatro son tipos primitivos y se consumen directamente.
     * fila_india es recursivo: fila_india ( TIPO ) permite arreglos de
     * cualquier tipo, incluyendo arreglos de arreglos.
     */
    private fun parseTipo() {
        when {
            check(TokenType.ENTERO) -> advance()
            check(TokenType.BOOLEANO) -> advance()
            check(TokenType.CADENA) -> advance()
            check(TokenType.VACIO) -> advance()
            check(TokenType.ARREGLO) -> {
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

    // producciones 10-14: parametros

    /**
     * PARAMS -> PARAM PARAMS' | epsilon
     *
     * si el token actual es un tipo, hay al menos un parametro.
     * si es ')' se deriva epsilon y no se consume nada.
     */
    private fun parseParams() {
        if (isTipoToken()) {
            parseParam()
            parseParamsPrima()
        }
        // epsilon: el token ')' cierra la lista vacia
    }

    /**
     * PARAMS' -> , PARAM PARAMS' | epsilon
     *
     * mientras haya comas hay mas parametros que parsear.
     */
    private fun parseParamsPrima() {
        while (check(TokenType.SEPARAR)) {
            advance()
            parseParam()
        }
        // epsilon: cualquier otro token termina la lista
    }

    /**
     * PARAM -> TIPO IDENT
     *
     * un parametro es un tipo seguido de un identificador.
     */
    private fun parseParam() {
        parseTipo()
        expect(TokenType.IDENT)
    }

    // producciones 15-18: bloque y sentencias

    /**
     * BLOQUE -> { SENTENCIAS }
     *
     * un bloque es una secuencia de sentencias entre llaves.
     */
    private fun parseBloque() {
        expect(TokenType.CORCHETE_ABIERTO)
        parseSentencias()
        expect(TokenType.CORCHETE_CERRADO)
    }

    /**
     * SENTENCIAS -> SENTENCIA SENTENCIAS'
     * SENTENCIAS' -> SENTENCIA SENTENCIAS' | epsilon
     *
     * se parsean sentencias mientras el token actual sea el inicio
     * de alguna sentencia valida. cuando se ve '}' se deriva epsilon.
     */
    private fun parseSentencias() {
        while (isSentenciaToken()) {
            try {
                parseSentencia()
            } catch (e: ParseException) {
                synchronize()
            }
        }
        // epsilon: '}' cierra el bloque
    }

    /**
     * SENTENCIA -> DECL_VAR | DECL_CONST | SENT_RETORNO | SENT_COND |
     *              SENT_CICLO | SENT_PRINT | SENT_BREAK | SENT_CONTINUE |
     *              SENT_EXPR
     *
     * el token actual determina de forma unica cual produccion aplicar,
     * lo cual es posible porque la gramatica es LL(1) y los conjuntos
     * FIRST de cada alternativa son disjuntos.
     */
    private fun parseSentencia() {
        when (peek().type) {
            TokenType.VARIABLE    -> parseDeclVar()
            TokenType.CONSTANTE   -> parseDeclConst()
            TokenType.RETORNAR    -> parseSentRetorno()
            TokenType.CONDICIONAL -> parseSentCond()
            TokenType.CICLO_INDEF -> parseSentCiclo()
            TokenType.IMPRIMIR    -> parseSentPrint()
            TokenType.BREAK       -> parseSentBreak()
            TokenType.CONTINUE    -> parseSentContinue()
            TokenType.IDENT       -> parseSentExpr()
            else -> throw reportError(
                "token inesperado '${peek().lexeme}' al inicio de sentencia " +
                "en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    // producciones 28-29: declaraciones de variable y constante

    /**
     * DECL_VAR -> vara TIPO IDENT = EXPRESION ;
     */
    private fun parseDeclVar() {
        expect(TokenType.VARIABLE)
        parseTipo()
        expect(TokenType.IDENT)
        expect(TokenType.OP_ASIG)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    /**
     * DECL_CONST -> de_fijo TIPO IDENT = EXPRESION ;
     */
    private fun parseDeclConst() {
        expect(TokenType.CONSTANTE)
        parseTipo()
        expect(TokenType.IDENT)
        expect(TokenType.OP_ASIG)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    // producciones 30-36: sentencias de control

    /**
     * SENT_RETORNO -> tomela EXPRESION ;
     */
    private fun parseSentRetorno() {
        expect(TokenType.RETORNAR)
        parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
    }

    /**
     * SENT_COND -> mae ( EXPRESION ) BLOQUE SINO_OPC
     */
    private fun parseSentCond() {
        expect(TokenType.CONDICIONAL)
        expect(TokenType.PAREN_ABIERTO)
        parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        parseBloque()
        parseSinoOpc()
    }

    /**
     * SINO_OPC -> tons BLOQUE | epsilon
     *
     * si el token actual es 'tons' se parsea la rama else.
     * cualquier otro token deriva epsilon.
     */
    private fun parseSinoOpc() {
        if (check(TokenType.SINO)) {
            advance()
            parseBloque()
        }
    }

    /**
     * SENT_CICLO -> bretee ( EXPRESION ) BLOQUE
     */
    private fun parseSentCiclo() {
        expect(TokenType.CICLO_INDEF)
        expect(TokenType.PAREN_ABIERTO)
        parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        parseBloque()
    }

    /**
     * SENT_BREAK -> jaleas ;
     */
    private fun parseSentBreak() {
        expect(TokenType.BREAK)
        expect(TokenType.FIN_SENTENCIA)
    }

    /**
     * SENT_CONTINUE -> dele_dele ;
     */
    private fun parseSentContinue() {
        expect(TokenType.CONTINUE)
        expect(TokenType.FIN_SENTENCIA)
    }

    // produccion 37: sentencia de impresion

    /**
     * SENT_PRINT -> miau ( ARGS ) ;
     */
    private fun parseSentPrint() {
        expect(TokenType.IMPRIMIR)
        expect(TokenType.PAREN_ABIERTO)
        parseArgs()
        expect(TokenType.PAREN_CERRADO)
        expect(TokenType.FIN_SENTENCIA)
    }

    // producciones 38-45: sentencias de expresion

    /**
     * SENT_EXPR -> IDENT SENT_IDENT_COLA
     */
    private fun parseSentExpr() {
        expect(TokenType.IDENT)
        parseSentIdentCola()
    }

    /**
     * SENT_IDENT_COLA -> = EXPRESION ;
     *                  | ( ARGS ) ;
     *                  | [ EXPRESION ] SENT_INDEX_COLA
     *                  | ++ ;
     *                  | -- ;
     */
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
                "'${tokens[current - 1].lexeme}' en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    /**
     * SENT_INDEX_COLA -> = EXPRESION ;
     *                  | [ EXPRESION ] SENT_INDEX_COLA
     */
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

    // producciones 46-49: argumentos

    /**
     * ARGS -> EXPRESION ARGS' | epsilon
     */
    private fun parseArgs() {
        if (isExpresionToken()) {
            parseExpresion()
            parseArgsPrima()
        }
    }

    /**
     * ARGS' -> , EXPRESION ARGS' | epsilon
     */
    private fun parseArgsPrima() {
        while (check(TokenType.SEPARAR)) {
            advance()
            parseExpresion()
        }
    }

    // producciones 50-96: expresiones

    /**
     * EXPRESION -> OR_EXPR
     *
     * punto de entrada para el parsing de cualquier expresion.
     * la jerarquia de precedencia se implementa mediante el encadenamiento
     * de metodos, donde cada nivel llama al nivel de mayor precedencia.
     */
    private fun parseExpresion() {
        parseOrExpr()
    }

    /**
     * OR_EXPR -> AND_EXPR OR_EXPR'
     * OR_EXPR' -> || AND_EXPR OR_EXPR' | epsilon
     */
    private fun parseOrExpr() {
        parseAndExpr()
        while (check(TokenType.OP_O)) {
            advance()
            parseAndExpr()
        }
    }

    /**
     * AND_EXPR -> EQUAL_EXPR AND_EXPR'
     * AND_EXPR' -> && EQUAL_EXPR AND_EXPR' | epsilon
     */
    private fun parseAndExpr() {
        parseEqualExpr()
        while (check(TokenType.OP_Y)) {
            advance()
            parseEqualExpr()
        }
    }

    /**
     * EQUAL_EXPR -> RELAC_EXPR EQUAL_EXPR'
     * EQUAL_EXPR' -> == RELAC_EXPR EQUAL_EXPR' | != RELAC_EXPR EQUAL_EXPR' | epsilon
     */
    private fun parseEqualExpr() {
        parseRelacExpr()
        while (check(TokenType.OP_IGU) || check(TokenType.OP_DIFE)) {
            advance()
            parseRelacExpr()
        }
    }

    /**
     * RELAC_EXPR -> ARIT_EXPR RELAC_EXPR'
     * RELAC_EXPR' -> < ARIT_EXPR | <= ARIT_EXPR | > ARIT_EXPR | >= ARIT_EXPR | epsilon
     */
    private fun parseRelacExpr() {
        parseAritExpr()
        while (peek().type in setOf(
            TokenType.OP_MENOR,
            TokenType.OP_MENOR_IG,
            TokenType.OP_MAYOR,
            TokenType.OP_MAYOR_IG
        )) {
            advance()
            parseAritExpr()
        }
    }

    /**
     * ARIT_EXPR -> TERMINO ARIT_EXPR'
     * ARIT_EXPR' -> + TERMINO ARIT_EXPR' | - TERMINO ARIT_EXPR' | epsilon
     */
    private fun parseAritExpr() {
        parseTermino()
        while (check(TokenType.OP_SUMA) || check(TokenType.OP_RESTA)) {
            advance()
            parseTermino()
        }
    }

    /**
     * TERMINO -> FACTOR TERMINO'
     * TERMINO' -> * FACTOR TERMINO' | / FACTOR TERMINO' | epsilon
     */
    private fun parseTermino() {
        parseFactor()
        while (check(TokenType.OP_MUL) || check(TokenType.OP_DIV)) {
            advance()
            parseFactor()
        }
    }

    /**
     * FACTOR -> ! FACTOR | - FACTOR | FACTOR_BASE FACTOR'
     *
     * los operadores unarios ! y - son prefijos y se aplican recursivamente,
     * lo que permite expresiones como !!condicion o --n (doble negacion).
     */
    private fun parseFactor() {
        when {
            check(TokenType.OP_NO) -> {
                advance()
                parseFactor()
            }
            check(TokenType.OP_RESTA) -> {
                advance()
                parseFactor()
            }
            else -> {
                parseFactorBase()
                parseFactorPrima()
            }
        }
    }

    /**
     * FACTOR_BASE -> ( EXPRESION )
     *              | [ LISTA_ELEMENTOS ]
     *              | IDENT
     *              | LIT_ENTERO
     *              | LIT_STRING
     *              | diay_si
     *              | diay_no
     *              | me_la_comi ( )
     */
    private fun parseFactorBase() {
        when (peek().type) {
            TokenType.PAREN_ABIERTO -> {
                // expresion agrupada: ( EXPRESION )
                advance()
                parseExpresion()
                expect(TokenType.PAREN_CERRADO)
            }
            TokenType.LLAVE_ABIERTA -> {
                // literal de arreglo: [ LISTA_ELEMENTOS ]
                advance()
                parseListaElementos()
                expect(TokenType.LLAVE_CERRADA)
            }
            TokenType.IDENT -> advance()
            TokenType.LIT_ENTERO -> advance()
            TokenType.LIT_STRING -> advance()
            TokenType.VERDADERO -> advance()
            TokenType.FALSO -> advance()
            TokenType.VACIO -> advance()
            TokenType.RECIBIR -> {
                // lectura de stdin: me_la_comi ( )
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

    /**
     * FACTOR' -> ++ | -- | ( ARGS ) | [ EXPRESION ] FACTOR_INDEX | epsilon
     *
     * los sufijos postfijos se aplican al factor base inmediatamente anterior.
     * ( ARGS ) representa una llamada a funcion cuando el factor base fue IDENT.
     * [ EXPRESION ] representa un acceso a elemento de arreglo.
     */
    private fun parseFactorPrima() {
        when (peek().type) {
            TokenType.OP_INC -> advance()
            TokenType.OP_DEC -> advance()
            TokenType.PAREN_ABIERTO -> {
                // llamada a funcion: f ( ARGS )
                advance()
                parseArgs()
                expect(TokenType.PAREN_CERRADO)
            }
            TokenType.LLAVE_ABIERTA -> {
                // acceso a arreglo: arr [ EXPRESION ] FACTOR_INDEX
                advance()
                parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseFactorIndex()
            }
            else -> {
                // epsilon: cualquier otro token no pertenece al factor prima
            }
        }
    }

    /**
     * FACTOR_INDEX -> [ EXPRESION ] FACTOR_INDEX | epsilon
     *
     * permite acceso a arreglos multidimensionales encadenados: matriz[i][j]
     */
    private fun parseFactorIndex() {
        if (check(TokenType.LLAVE_ABIERTA)) {
            advance()
            parseExpresion()
            expect(TokenType.LLAVE_CERRADA)
            parseFactorIndex()
        }
        // epsilon: cualquier otro token termina la cadena de indices
    }

    /**
     * LISTA_ELEMENTOS -> EXPRESION LISTA_ELEMENTOS' | epsilon
     * LISTA_ELEMENTOS' -> , EXPRESION LISTA_ELEMENTOS' | epsilon
     *
     * lista de elementos para inicializar un arreglo literal: [1, 2, 3]
     */
    private fun parseListaElementos() {
        if (isExpresionToken()) {
            parseExpresion()
            while (check(TokenType.SEPARAR)) {
                advance()
                parseExpresion()
            }
        }
        // epsilon: ] vacio es un arreglo vacio
    }
    // utilidades de clasificacion de tokens

    /**
     * retorna true si el token actual puede iniciar un tipo.
     * corresponde al conjunto FIRST(TIPO).
     */
    private fun isTipoToken(): Boolean = peek().type in setOf(
        TokenType.ENTERO,
        TokenType.BOOLEANO,
        TokenType.CADENA,
        TokenType.VACIO,
        TokenType.ARREGLO
    )

    /**
     * retorna true si el token actual puede iniciar una sentencia.
     * corresponde al conjunto FIRST(SENTENCIA).
     */
    private fun isSentenciaToken(): Boolean = peek().type in setOf(
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

    /**
     * retorna true si el token actual puede iniciar una expresion.
     * corresponde al conjunto FIRST(EXPRESION) de la gramatica.
     */
    private fun isExpresionToken(): Boolean = peek().type in setOf(
        TokenType.OP_NO,
        TokenType.OP_RESTA,
        TokenType.PAREN_ABIERTO,
        TokenType.LLAVE_ABIERTA,
        TokenType.IDENT,
        TokenType.LIT_ENTERO,
        TokenType.LIT_STRING,
        TokenType.VERDADERO,
        TokenType.FALSO,
        TokenType.RECIBIR
    )
}