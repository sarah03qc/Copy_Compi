package edu.ic5701.parser

import edu.ic5701.ast.nodes.*
import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType

/**
 * parser de descenso recursivo para el lenguaje CR++.
 *
 * recibe la lista de tokens producida por el scanner y construye el AST
 * correspondiente segun la gramatica LL(1) documentada en docs/03 GRAMATICA.md.
 * cada no-terminal de la gramatica tiene su propio metodo privado que retorna
 * el nodo AST correspondiente.
 *
 * las responsabilidades de navegacion de tokens y reporte de errores se delegan
 * a TokenStream y ErrorReporter respectivamente.
 */
class RecursiveDescentParser(tokens: List<Token>) : Parser {

    private val stream = TokenStream(tokens)
    private val reporter = ErrorReporter(stream)

    override val errors: List<String> get() = reporter.errors

    // -----------------------------------------------------------
    // punto de entrada
    // -----------------------------------------------------------

    override fun parse(): Programa? {
        val programa = parsePrograma()
        return if (errors.isEmpty()) programa else null
    }

    // -----------------------------------------------------------
    // utilitarios locales
    // -----------------------------------------------------------

    private fun peek(): Token = stream.peek()

    private fun check(type: TokenType): Boolean = stream.check(type)

    private fun advance(): Token = stream.advance()

    private fun expect(type: TokenType): Token {
        if (check(type)) return advance()
        throw reporter.expectedError(type)
    }

    private fun tokenIndex(): Int = stream.currentIndex()

    private fun reportError(message: String): ParseException =
        reporter.reportError(message)

    private fun synchronize() = reporter.synchronize()

    // -----------------------------------------------------------
    // producciones 1-3: programa
    // -----------------------------------------------------------

    private fun parsePrograma(): Programa {
        val inicio = tokenIndex()
        val funciones = mutableListOf<Declaracion>()

        try {
            funciones.add(parseDeclFunc())
        } catch (e: ParseException) {
            synchronize()
        }

        while (check(TokenType.FUNCION)) {
            try {
                funciones.add(parseDeclFunc())
            } catch (e: ParseException) {
                synchronize()
            }
        }

        try {
            expect(TokenType.EOF)
        } catch (e: ParseException) {
            // error ya registrado
        }

        return Programa(
            inicio = inicio,
            fin = tokenIndex(),
            cuerpo = funciones
        )
    }

    // -----------------------------------------------------------
    // produccion 4: declaracion de funcion
    // -----------------------------------------------------------

    private fun parseDeclFunc(): FuncionDecl {
        val inicio = tokenIndex()
        expect(TokenType.FUNCION)
        val retorno = parseTipo()
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        expect(TokenType.PAREN_ABIERTO)
        val params = parseParams()
        expect(TokenType.PAREN_CERRADO)
        val cuerpo = parseBloque()
        expect(TokenType.DOS_PUNTOS)
        parseTipo() // tipo de retorno al final, ya lo capturamos antes

        val esEntrada = nombre.nombre == "chante"

        return FuncionDecl(
            inicio = inicio,
            fin = tokenIndex(),
            nombre = nombre,
            esEntrada = esEntrada,
            retorno = retorno,
            params = params,
            cuerpo = cuerpo
        )
    }

    // -----------------------------------------------------------
    // producciones 5-9: tipos
    // -----------------------------------------------------------

    private fun parseTipo(): TipoDato {
        val inicio = tokenIndex()
        return when {
            check(TokenType.ENTERO) -> {
                advance()
                TipoColones(inicio, tokenIndex())
            }
            check(TokenType.BOOLEANO) -> {
                advance()
                TipoDiay(inicio, tokenIndex())
            }
            check(TokenType.CADENA) -> {
                advance()
                TipoLabia(inicio, tokenIndex())
            }
            check(TokenType.VACIO) -> {
                advance()
                TipoNiPapa(inicio, tokenIndex())
            }
            check(TokenType.ARREGLO) -> {
                advance()
                expect(TokenType.PAREN_ABIERTO)
                val tipoElemento = parseTipo()
                expect(TokenType.PAREN_CERRADO)
                TipoArray(inicio, tokenIndex(), tipoElemento)
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

    private fun parseParams(): List<Parametro> {
        val params = mutableListOf<Parametro>()
        if (isTipoToken()) {
            params.add(parseParam())
            while (check(TokenType.SEPARAR)) {
                advance()
                params.add(parseParam())
            }
        }
        return params
    }

    private fun parseParam(): Parametro {
        val inicio = tokenIndex()
        val tipo = parseTipo()
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        return Parametro(inicio, tokenIndex(), tipo, nombre)
    }

    // -----------------------------------------------------------
    // producciones 15-18: bloque y sentencias
    // -----------------------------------------------------------

    private fun parseBloque(): Bloque {
        val inicio = tokenIndex()
        expect(TokenType.CORCHETE_ABIERTO)
        val sentencias = mutableListOf<ASTNode>()
        while (isSentenciaToken()) {
            try {
                sentencias.add(parseSentencia())
            } catch (e: ParseException) {
                synchronize()
            }
        }
        expect(TokenType.CORCHETE_CERRADO)
        return Bloque(inicio, tokenIndex(), sentencias)
    }

    // -----------------------------------------------------------
    // producciones 19-27: dispatcher de sentencias
    // -----------------------------------------------------------

    private fun parseSentencia(): ASTNode {
        return when (peek().type) {
            TokenType.VARIABLE    -> parseDeclVar()
            TokenType.CONSTANTE   -> parseDeclConst()
            TokenType.RETORNAR    -> parseSentRetorno()
            TokenType.CONDICIONAL -> parseSentCond()
            TokenType.CICLO_INDEF -> parseSentCiclo()
            TokenType.IMPRIMIR    -> parseSentPrint()
            TokenType.BREAK       -> parseSentBreak()
            TokenType.CONTINUE    -> parseSentContinue()
            TokenType.IDENT       -> parseSentExpr()
            TokenType.OP_INC      -> parseSentPrefijoInc()
            TokenType.OP_DEC      -> parseSentPrefijoDec()
            else -> throw reportError(
                "token inesperado '${peek().lexeme}' al inicio de sentencia " +
                "en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    // -----------------------------------------------------------
    // producciones 28-29: declaraciones de variable y constante
    // -----------------------------------------------------------

    private fun parseDeclVar(): VariableDecl {
        val inicio = tokenIndex()
        expect(TokenType.VARIABLE)
        val tipo = parseTipo()
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        expect(TokenType.OP_ASIG)
        val valor = parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
        return VariableDecl(inicio, tokenIndex(), nombre, tipo, valor)
    }

    private fun parseDeclConst(): ConstanteDecl {
        val inicio = tokenIndex()
        expect(TokenType.CONSTANTE)
        val tipo = parseTipo()
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        expect(TokenType.OP_ASIG)
        val valor = parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
        return ConstanteDecl(inicio, tokenIndex(), nombre, tipo, valor)
    }

    // -----------------------------------------------------------
    // producciones 30-36: sentencias de control
    // -----------------------------------------------------------

    private fun parseSentRetorno(): Retorno {
        val inicio = tokenIndex()
        expect(TokenType.RETORNAR)
        val valor = parseExpresion()
        expect(TokenType.FIN_SENTENCIA)
        return Retorno(inicio, tokenIndex(), valor)
    }

    private fun parseSentCond(): Condicional {
        val inicio = tokenIndex()
        expect(TokenType.CONDICIONAL)
        expect(TokenType.PAREN_ABIERTO)
        val condicion = parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        val thenBody = parseBloque()
        val elseBody = if (check(TokenType.SINO)) {
            advance()
            parseBloque()
        } else null
        return Condicional(inicio, tokenIndex(), condicion, thenBody, elseBody)
    }

    private fun parseSentCiclo(): Ciclo {
        val inicio = tokenIndex()
        expect(TokenType.CICLO_INDEF)
        expect(TokenType.PAREN_ABIERTO)
        val condicion = parseExpresion()
        expect(TokenType.PAREN_CERRADO)
        val cuerpo = parseBloque()
        return Ciclo(inicio, tokenIndex(), condicion, cuerpo)
    }

    private fun parseSentBreak(): Ruptura {
        val inicio = tokenIndex()
        expect(TokenType.BREAK)
        expect(TokenType.FIN_SENTENCIA)
        return Ruptura(inicio, tokenIndex())
    }

    private fun parseSentContinue(): Continuar {
        val inicio = tokenIndex()
        expect(TokenType.CONTINUE)
        expect(TokenType.FIN_SENTENCIA)
        return Continuar(inicio, tokenIndex())
    }

    // -----------------------------------------------------------
    // produccion 37: sentencia de impresion
    // -----------------------------------------------------------

    private fun parseSentPrint(): Imprimir {
        val inicio = tokenIndex()
        expect(TokenType.IMPRIMIR)
        expect(TokenType.PAREN_ABIERTO)
        val args = parseArgs()
        expect(TokenType.PAREN_CERRADO)
        expect(TokenType.FIN_SENTENCIA)
        return Imprimir(inicio, tokenIndex(), args)
    }

    // -----------------------------------------------------------
    // producciones 38-45: sentencias de expresion
    // -----------------------------------------------------------

    private fun parseSentExpr(): ASTNode {
        val inicio = tokenIndex()
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)

        return when (peek().type) {
            TokenType.OP_ASIG -> {
                advance()
                val valor = parseExpresion()
                expect(TokenType.FIN_SENTENCIA)
                AsignacionSimple(inicio, tokenIndex(), nombre, valor)
            }
            TokenType.PAREN_ABIERTO -> {
                advance()
                val args = parseArgs()
                expect(TokenType.PAREN_CERRADO)
                expect(TokenType.FIN_SENTENCIA)
                SentenciaExpr(inicio, tokenIndex(), LlamadaFuncion(inicio, tokenIndex(), nombre, args))
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                val indice = parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseSentIndexCola(inicio, nombre, indice)
            }
            TokenType.OP_INC -> {
                advance()
                expect(TokenType.FIN_SENTENCIA)
                SentenciaExpr(inicio, tokenIndex(), OperacionAum(inicio, tokenIndex(), nombre))
            }
            TokenType.OP_DEC -> {
                advance()
                expect(TokenType.FIN_SENTENCIA)
                SentenciaExpr(inicio, tokenIndex(), OperacionDec(inicio, tokenIndex(), nombre))
            }
            else -> throw reportError(
                "se esperaba '=', '(', '[', '++' o '--' despues del identificador " +
                "'${nombre.nombre}' en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    private fun parseSentIndexCola(inicio: Int, nombre: Identificador, indice: Expresion): ASTNode {
        return when (peek().type) {
            TokenType.OP_ASIG -> {
                advance()
                val valor = parseExpresion()
                expect(TokenType.FIN_SENTENCIA)
                AsignacionArray(inicio, tokenIndex(), nombre, indice, valor)
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                val nuevoIndice = parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                parseSentIndexCola(inicio, nombre, AccesoArray(inicio, tokenIndex(), nombre, nuevoIndice))
            }
            else -> throw reportError(
                "se esperaba '=' o '[' despues del acceso al arreglo " +
                "en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    private fun parseSentPrefijoInc(): SentenciaExpr {
        val inicio = tokenIndex()
        expect(TokenType.OP_INC)
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        expect(TokenType.FIN_SENTENCIA)
        return SentenciaExpr(inicio, tokenIndex(), OperacionAum(inicio, tokenIndex(), nombre))
    }

    private fun parseSentPrefijoDec(): SentenciaExpr {
        val inicio = tokenIndex()
        expect(TokenType.OP_DEC)
        val nombreToken = expect(TokenType.IDENT)
        val nombre = Identificador(nombreToken.line, nombreToken.column, nombreToken.lexeme)
        expect(TokenType.FIN_SENTENCIA)
        return SentenciaExpr(inicio, tokenIndex(), OperacionDec(inicio, tokenIndex(), nombre))
    }

    // -----------------------------------------------------------
    // producciones 46-49: argumentos
    // -----------------------------------------------------------

    private fun parseArgs(): List<Expresion> {
        val args = mutableListOf<Expresion>()
        if (isExpresionToken()) {
            args.add(parseExpresion())
            while (check(TokenType.SEPARAR)) {
                advance()
                args.add(parseExpresion())
            }
        }
        return args
    }

    // -----------------------------------------------------------
    // producciones 50-96: expresiones
    // -----------------------------------------------------------

    private fun parseExpresion(): Expresion = parseOrExpr()

    private fun parseOrExpr(): Expresion {
        val inicio = tokenIndex()
        var izq = parseAndExpr()
        while (check(TokenType.OP_O)) {
            advance()
            val der = parseAndExpr()
            izq = OperacionO(inicio, tokenIndex(), izq, der)
        }
        return izq
    }

    private fun parseAndExpr(): Expresion {
        val inicio = tokenIndex()
        var izq = parseEqualExpr()
        while (check(TokenType.OP_Y)) {
            advance()
            val der = parseEqualExpr()
            izq = OperacionY(inicio, tokenIndex(), izq, der)
        }
        return izq
    }

    private fun parseEqualExpr(): Expresion {
        val inicio = tokenIndex()
        var izq = parseRelacExpr()
        while (stream.checkAny(TokenType.OP_IGU, TokenType.OP_DIFE)) {
            val op = advance()
            val der = parseRelacExpr()
            izq = if (op.type == TokenType.OP_IGU)
                OperacionIgu(inicio, tokenIndex(), izq, der)
            else
                OperacionDif(inicio, tokenIndex(), izq, der)
        }
        return izq
    }

    private fun parseRelacExpr(): Expresion {
        val inicio = tokenIndex()
        var izq = parseAritExpr()
        while (stream.checkAny(
            TokenType.OP_MENOR, TokenType.OP_MENOR_IG,
            TokenType.OP_MAYOR, TokenType.OP_MAYOR_IG
        )) {
            val op = advance()
            val der = parseAritExpr()
            izq = when (op.type) {
                TokenType.OP_MENOR    -> OperacionMen(inicio, tokenIndex(), izq, der)
                TokenType.OP_MENOR_IG -> OperacionMenIgu(inicio, tokenIndex(), izq, der)
                TokenType.OP_MAYOR    -> OperacionMay(inicio, tokenIndex(), izq, der)
                else                  -> OperacionMayIgu(inicio, tokenIndex(), izq, der)
            }
        }
        return izq
    }

    private fun parseAritExpr(): Expresion {
        val inicio = tokenIndex()
        var izq = parseTermino()
        while (stream.checkAny(TokenType.OP_SUMA, TokenType.OP_RESTA)) {
            val op = advance()
            val der = parseTermino()
            izq = if (op.type == TokenType.OP_SUMA)
                OperacionSuma(inicio, tokenIndex(), izq, der)
            else
                OperacionResta(inicio, tokenIndex(), izq, der)
        }
        return izq
    }

    private fun parseTermino(): Expresion {
        val inicio = tokenIndex()
        var izq = parseFactor()
        while (stream.checkAny(TokenType.OP_MUL, TokenType.OP_DIV)) {
            val op = advance()
            val der = parseFactor()
            izq = if (op.type == TokenType.OP_MUL)
                OperacionMul(inicio, tokenIndex(), izq, der)
            else
                OperacionDiv(inicio, tokenIndex(), izq, der)
        }
        return izq
    }

    private fun parseFactor(): Expresion {
        val inicio = tokenIndex()
        return when {
            check(TokenType.OP_NO) -> {
                advance()
                OperacionDec(inicio, tokenIndex(), parseFactor()) 
            }
            check(TokenType.OP_RESTA) -> {
                advance()
                OperacionAum(inicio, tokenIndex(), parseFactor()) 
            }
            else -> {
                val base = parseFactorBase()
                parseFactorPrima(inicio, base)
            }
        }
    }

    private fun parseFactorBase(): Expresion {
        val inicio = tokenIndex()
        return when (peek().type) {
            TokenType.PAREN_ABIERTO -> {
                advance()
                val expr = parseExpresion()
                expect(TokenType.PAREN_CERRADO)
                expr
            }
            TokenType.LLAVE_ABIERTA -> {
                advance()
                val elementos = parseListaElementos()
                expect(TokenType.LLAVE_CERRADA)
                InicializarArray(inicio, tokenIndex(), elementos)
            }
            TokenType.IDENT -> {
                val t = advance()
                Identificador(t.line, t.column, t.lexeme)
            }
            TokenType.LIT_ENTERO -> {
                val t = advance()
                LiteralEntero(t.line, t.column, t.lexeme.toInt())
            }
            TokenType.LIT_STRING -> {
                val t = advance()
                val valor = t.lexeme.removeSurrounding("\"")
                LiteralCadena(t.line, t.column, valor)
            }
            TokenType.VERDADERO -> {
                val t = advance()
                LiteralBooleano(t.line, t.column, true)
            }
            TokenType.FALSO -> {
                val t = advance()
                LiteralBooleano(t.line, t.column, false)
            }
            TokenType.VACIO -> {
                val t = advance()
                LiteralNulo(t.line, t.column)
            }
            TokenType.RECIBIR -> {
                val t = advance()
                expect(TokenType.PAREN_ABIERTO)
                expect(TokenType.PAREN_CERRADO)
                Leer(t.line, t.column)
            }
            else -> throw reportError(
                "se esperaba una expresion pero se encontro '${peek().lexeme}' " +
                "(${peek().type.name}) en linea ${peek().line}, columna ${peek().column}"
            )
        }
    }

    private fun parseFactorPrima(inicio: Int, base: Expresion): Expresion {
        return when (peek().type) {
            TokenType.OP_INC -> {
                advance()
                OperacionAum(inicio, tokenIndex(), base)
            }
            TokenType.OP_DEC -> {
                advance()
                OperacionDec(inicio, tokenIndex(), base)
            }
            TokenType.PAREN_ABIERTO -> {
                if (base !is Identificador) throw reportError(
                    "solo se puede llamar a una funcion por nombre"
                )
                advance()
                val args = parseArgs()
                expect(TokenType.PAREN_CERRADO)
                LlamadaFuncion(inicio, tokenIndex(), base, args)
            }
            TokenType.LLAVE_ABIERTA -> {
                if (base !is Identificador) throw reportError(
                    "solo se puede acceder a un arreglo por nombre"
                )
                advance()
                val indice = parseExpresion()
                expect(TokenType.LLAVE_CERRADA)
                var acceso: Expresion = AccesoArray(inicio, tokenIndex(), base, indice)
                acceso = parseFactorIndex(inicio, acceso)
                acceso
            }
            else -> base
        }
    }

    private fun parseFactorIndex(inicio: Int, base: Expresion): Expresion {
        if (!check(TokenType.LLAVE_ABIERTA)) return base
        advance()
        val indice = parseExpresion()
        expect(TokenType.LLAVE_CERRADA)
        val acceso = if (base is AccesoArray)
            AccesoArray(inicio, tokenIndex(), base.nombre, indice)
        else
            base
        return parseFactorIndex(inicio, acceso)
    }

    private fun parseListaElementos(): List<Expresion> {
        val elementos = mutableListOf<Expresion>()
        if (isExpresionToken()) {
            elementos.add(parseExpresion())
            while (check(TokenType.SEPARAR)) {
                advance()
                elementos.add(parseExpresion())
            }
        }
        return elementos
    }

    // -----------------------------------------------------------
    // utilidades de clasificacion de tokens
    // -----------------------------------------------------------

    private fun isTipoToken(): Boolean = stream.checkAny(
        TokenType.ENTERO, TokenType.BOOLEANO, TokenType.CADENA,
        TokenType.VACIO, TokenType.ARREGLO
    )

    private fun isSentenciaToken(): Boolean = stream.checkAny(
        TokenType.VARIABLE, TokenType.CONSTANTE, TokenType.RETORNAR,
        TokenType.CONDICIONAL, TokenType.CICLO_INDEF, TokenType.IMPRIMIR,
        TokenType.BREAK, TokenType.CONTINUE, TokenType.IDENT,
        TokenType.OP_INC, TokenType.OP_DEC
    )

    private fun isExpresionToken(): Boolean = stream.checkAny(
        TokenType.OP_NO, TokenType.OP_RESTA, TokenType.PAREN_ABIERTO,
        TokenType.LLAVE_ABIERTA, TokenType.IDENT, TokenType.LIT_ENTERO,
        TokenType.LIT_STRING, TokenType.VERDADERO, TokenType.FALSO,
        TokenType.VACIO, TokenType.RECIBIR
    )
}