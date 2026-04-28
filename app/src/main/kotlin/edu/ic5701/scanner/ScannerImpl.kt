package edu.ic5701.scanner

import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType

/**
 * Scanner (analizador léxico) para el lenguaje CR++.
 *
 * Implementa el autómata de transiciones definido en docs/02_LÉXICO.plantuml.
 * Soporta rollback (retroceso de un carácter) cuando el DFA llega a un estado
 * de aceptación que lo requiere (p.ej. IDENT, LIT_ENTERO, OP_RESTA, OP_ASIG, etc.).
 *
 * El scanner omite espacios en blanco, tabs, retornos de línea y saltos de línea.
 * En caso de carácter inesperado emite un token ERROR y reporta el error léxico.
 */
class ScannerImpl (private val source: String) : Scanner {
    companion object TokenHelper{
        private const val UNDERSCORED = '_'
        private val KEYWORDS: Map<String, TokenType> = mapOf(
            "chunche"    to TokenType.FUNCION,
            "vara"       to TokenType.VARIABLE,
            "de_fijo"    to TokenType.CONSTANTE,
            "colones"    to TokenType.ENTERO,
            "diay"       to TokenType.BOOLEANO,
            "labia"      to TokenType.CADENA,
            "ni_papa"    to TokenType.VACIO,
            "fila_india" to TokenType.ARREGLO,
            "mae"        to TokenType.CONDICIONAL,
            "tons"       to TokenType.SINO,
            "bretee"     to TokenType.CICLO_INDEF,
            "tomela"     to TokenType.RETORNAR,   // también tómela
            "miau"       to TokenType.IMPRIMIR,
            "me_la_comi" to TokenType.RECIBIR,    // también me_la_comí
            "jaleas"     to TokenType.BREAK,
            "dele_dele"  to TokenType.CONTINUE,
            "diay_si"    to TokenType.VERDADERO,  // también diay_sí
            "diay_no"    to TokenType.FALSO
        )
        // Tabla de palabras reservadas (normaliza tildes antes de buscar)
        /**
         * Normaliza caracteres acentuados a su equivalente sin tilde para
         * búsqueda de keywords. Solo normaliza las vocales con acento y la ü.
         * No modifica caracteres que pertenecen al cuerpo de identificadores válidos.
         */
        fun normalizeAccents(s: String): String = buildString {
            for (c in s) {
                append(
                    when (c) {
                        'á', 'Á' -> 'a'
                        'é', 'É' -> 'e'
                        'í', 'Í' -> 'i'
                        'ó', 'Ó' -> 'o'
                        'ú', 'Ú' -> 'u'
                        else -> c
                    }
                )
            }
        }

        /** Determina si un carácter puede ser el primer carácter de un IDENT. */
        fun isIdentStart(c: Char): Boolean =
            c.isLetter() || c == UNDERSCORED || isAccented(c)

        /** Determina si un carácter puede continuar un IDENT. */
        fun isIdentPart(c: Char): Boolean =
            isIdentStart(c) || c.isDigit()

        /** Carácter acentuado válido en identificadores (español + diéresis). */
        private fun isAccented(c: Char): Boolean = c in setOf(
            'á', 'ä', 'Á', 'Ä',
            'é', 'ë', 'É', 'Ë',
            'í', 'ï', 'Í', 'Ï',
            'ó', 'ö', 'Ó', 'Ö',
            'ú', 'ü', 'Ú', 'Ü',
            'ñ', 'Ñ'
        )
    }

    /** Lista de errores léxicos encontrados. */
    val errors: MutableList<String> = mutableListOf()

    // Estado interno del scanner
    private val firstColumn = 1
    private val indexOutOfRange = -1

    private object Symbol {
        const val T = 't'
        const val N = 'n'
        const val R = 'r'
        const val QUOTE = '"'
        const val COLON = ':'
        const val ASIGNATE_RETURN_TYPE = ":"
        const val COMMA = ','
        const val SEPARATE = ","
        const val SEMICOLON = ';'
        const val END_SENTENCE = ";"
        const val OPEN_PAREN = '('
        const val START_PAREN = "("
        const val CLOSE_PAREN = ')'
        const val END_PAREN = ")"
        const val OPEN_BRACKET = '['
        const val START_BRACKET = "["
        const val CLOSE_BRACKET = ']'
        const val END_BRACKET = "]"
        const val OPEN_BRACE = '{'
        const val START_BRACE = "{"
        const val CLOSE_BRACE = '}'
        const val END_BRACE = "}"
        const val PLUS = '+'
        const val INCREMENT = "++"
        const val ADD = "+"
        const val MINUS = '-'
        const val DECREMENT = "--"
        const val SUBTRACT = "-"
        const val ASTERISK = '*'
        const val MULTIPLY = "*"
        const val NORMAL_BAR = '/'
        const val DIVIDE = "/"
        const val OPEN_ANGULAR = '<'
        const val LESS_THAN = "<"
        const val LESS_EQUALS = "<="
        const val CLOSED_ANGULAR = '>'
        const val GREATER_THAN = ">"
        const val GREATER_EQUALS = ">="
        const val EQUALS = '='
        const val ASSIGNATION = "="
        const val SAME = "=="
        const val DIFFERENT = "!="
        const val EXCLAMATION_MARK = '!'
        const val NOT = "!"
        const val AMPERSAND = '&'
        const val AND = "&&"
        const val PIPE = '|'
        const val OR = "||"
        const val TAB = '\t'
        const val NEW_LINE = '\n'
        const val RETURN = '\r'
        const val INVERTED_BAR = '\\'
        const val WHITESPACE_VALUES = " $NEW_LINE$RETURN$TAB"
        const val EOF = '\u0004'
    }

    private var pos: Int = 0       // posición actual en source
    private var line: Int = 1      // línea actual (1-based)
    private var column: Int = firstColumn    // columna actual (1-based)



    // API pública
    /** Escanea el archivo y retorna la lista de tokens ignorando los espacios. */
    override fun scanAll(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (true) {
            val tok = nextToken()
            tokens.add(tok)
            if (tok.type == TokenType.EOF) break
        }
        return tokens
    }

    /** Retorna el siguiente token del flujo de entrada. */
    private fun nextToken(): Token {
        // Saltar espacios en blanco
        skipWhitespace()

        if (pos >= source.length) {
            return Token(TokenType.EOF, Symbol.EOF + "", line, column)
        }

        val startLine = line
        val startCol  = column
        val ch = peek()

        return when {
            // Fila 0: IDENT / Keyword  (estado 1R → 1 → 2)
            isIdentStart(ch) -> scanIdent(startLine, startCol)

            // Fila 0: LIT_STRING  (estado 3R → 3 → 4)
            ch == Symbol.QUOTE -> scanString(startLine, startCol)

            // Fila 1: LIT_ENTERO  (estado C1 → 5 → 6)
            ch.isDigit() -> scanInteger(startLine, startCol)

            // Fila 1: OP_DEC / OP_RESTA  (estado 7 → 8 | 9)
            ch == Symbol.MINUS -> {
                advance()
                if (pos < source.length && peek() == Symbol.MINUS) {
                    advance()
                    Token(TokenType.OP_DEC, Symbol.DECREMENT, startLine, startCol)
                } else {
                    Token(TokenType.OP_RESTA, Symbol.SUBTRACT, startLine, startCol)
                }
            }

            // Fila 2: OP_INC / OP_SUMA  (estado 10 → 11 | 12)
            ch == Symbol.PLUS -> {
                advance()
                if (pos < source.length && peek() == Symbol.PLUS) {
                    advance()
                    Token(TokenType.OP_INC, Symbol.INCREMENT, startLine, startCol)
                } else {
                    Token(TokenType.OP_SUMA, Symbol.ADD, startLine, startCol)
                }
            }

            // Fila 2: OP_MUL  (estado 13)
            ch == Symbol.ASTERISK -> { advance(); Token(TokenType.OP_MUL, Symbol.MULTIPLY, startLine, startCol) }

            // Fila 3: OP_DIV  (estado 14)
            ch == Symbol.NORMAL_BAR -> { advance(); Token(TokenType.OP_DIV, Symbol.DIVIDE, startLine, startCol) }

            // Fila 3: OP_IGU / OP_ASIG  (estado 15 → 16 | 17)
            ch == Symbol.EQUALS -> {
                advance()
                if (pos < source.length && peek() == Symbol.EQUALS) {
                    advance()
                    Token(TokenType.OP_IGU, Symbol.SAME, startLine, startCol)
                } else {
                    Token(TokenType.OP_ASIG, Symbol.ASSIGNATION, startLine, startCol)
                }
            }

            // Fila 4: OP_DIFE / OP_NO  (estado 18 → 19 | 20)
            ch == Symbol.EXCLAMATION_MARK -> {
                advance()
                if (pos < source.length && peek() == Symbol.EQUALS) {
                    advance()
                    Token(TokenType.OP_DIFE, Symbol.DIFFERENT, startLine, startCol)
                } else {
                    Token(TokenType.OP_NO, Symbol.NOT, startLine, startCol)
                }
            }

            // Fila 4: OP_MAYOR_IG / OP_MAYOR  (estado 21 → 22 | 23)
            ch == Symbol.CLOSED_ANGULAR -> {
                advance()
                if (pos < source.length && peek() == Symbol.EQUALS) {
                    advance()
                    Token(TokenType.OP_MAYOR_IG, Symbol.GREATER_EQUALS, startLine, startCol)
                } else {
                    Token(TokenType.OP_MAYOR, Symbol.GREATER_THAN, startLine, startCol)
                }
            }

            // Fila 5: OP_Y  (estado 24 → 25)
            ch == Symbol.AMPERSAND -> {
                advance()
                if (pos < source.length && peek() == Symbol.AMPERSAND) {
                    advance()
                    Token(TokenType.OP_Y, Symbol.AND, startLine, startCol)
                } else {
                    val bad = Symbol.AMPERSAND + peek().toString()
                    reportError(startLine, startCol, "carácter inesperado: '$bad' (se esperaba '&&')")
                    Token(TokenType.ERROR, bad, startLine, startCol)
                }
            }

            // Fila 5: OP_MENOR_IG / OP_MENOR  (estado 26 → 27 | 28)
            ch == Symbol.OPEN_ANGULAR -> {
                advance()
                if (pos < source.length && peek() == Symbol.EQUALS) {
                    advance()
                    Token(TokenType.OP_MENOR_IG, Symbol.LESS_EQUALS, startLine, startCol)
                } else {
                    Token(TokenType.OP_MENOR, Symbol.LESS_THAN, startLine, startCol)
                }
            }

            // Fila 6: OP_O  (estado 29 → 30)
            ch == Symbol.PIPE -> {
                advance()
                if (pos < source.length && peek() == Symbol.PIPE) {
                    advance()
                    Token(TokenType.OP_O, Symbol.OR, startLine, startCol)
                } else {
                    val bad = Symbol.PIPE + peek().toString()
                    reportError(startLine, startCol, "carácter inesperado: '$bad' (se esperaba '||')")
                    Token(TokenType.ERROR, bad, startLine, startCol)
                }
            }

            // Fila 6: PAREN_ABIERTO  (estado 31)
            ch == Symbol.OPEN_PAREN -> { advance(); Token(TokenType.PAREN_ABIERTO, Symbol.START_PAREN, startLine, startCol) }

            // Fila 7: PAREN_CERRADO  (estado 32)
            ch == Symbol.CLOSE_PAREN -> { advance(); Token(TokenType.PAREN_CERRADO, Symbol.END_PAREN, startLine, startCol) }

            // Fila 7: LLAVE_ABIERTA — nota: en el DFA el '[' mapea a LLAVE_ABIERTA
            // y el '{' mapea a CORCHETE_ABIERTO (nomenclatura del diagrama).
            // Aquí lo mantenemos fiel al diagrama: '[' → LLAVE_ABIERTA
            //                                       '{' → CORCHETE_ABIERTO
            ch == Symbol.OPEN_BRACKET -> { advance(); Token(TokenType.LLAVE_ABIERTA, Symbol.START_BRACKET, startLine, startCol) }

            // Fila 8: LLAVE_CERRADA  (estado 34)  ']' → LLAVE_CERRADA
            ch == Symbol.CLOSE_BRACKET -> { advance(); Token(TokenType.LLAVE_CERRADA, Symbol.END_BRACKET, startLine, startCol) }

            // Fila 8: CORCHETE_ABIERTO  (estado 35) '{' → CORCHETE_ABIERTO
            ch == Symbol.OPEN_BRACE -> { advance(); Token(TokenType.CORCHETE_ABIERTO, Symbol.START_BRACE, startLine, startCol) }

            // Fila 9: CORCHETE_CERRADO  (estado 36) '}' → CORCHETE_CERRADO
            ch == Symbol.CLOSE_BRACE -> { advance(); Token(TokenType.CORCHETE_CERRADO, Symbol.END_BRACE, startLine, startCol) }

            // Fila 9: FIN_SENTENCIA  (estado 37)
            ch == Symbol.SEMICOLON -> { advance(); Token(TokenType.FIN_SENTENCIA, Symbol.END_SENTENCE, startLine, startCol) }

            // Fila 10: SEPARAR  (estado 38)
            ch == Symbol.COMMA -> { advance(); Token(TokenType.SEPARAR, Symbol.SEPARATE, startLine, startCol) }

            // Fila 10: DOS_PUNTOS  (estado 39)
            ch == Symbol.COLON -> { advance(); Token(TokenType.DOS_PUNTOS, Symbol.ASIGNATE_RETURN_TYPE, startLine, startCol) }

            // Error léxico: carácter no reconocido
            else -> {
                advance()
                val bad = ch.toString()
                reportError(startLine, startCol, "carácter no reconocido: '$bad' (U+${ch.code.toString(16).uppercase()})")
                Token(TokenType.ERROR, bad, startLine, startCol)
            }
        }
    }

    // Métodos auxiliares de escaneo
    /**
     * Escanea un IDENT o keyword (estados C0 → 1 → 2).
     * Aplica rollback implícito: el estado 2 detiene la lectura sin consumir
     * el carácter 'otro' porque el while ya lo dejó fuera.
     */
    private fun scanIdent(startLine: Int, startCol: Int): Token {
        val sb = StringBuilder()
        // Estado 1: leer mientras sea parte de identificador
        while (pos < source.length && isIdentPart(peek())) {
            sb.append(advance())
        }
        // Estado 2: IDENT con rollback (el próximo carácter NO fue consumido)
        val raw = sb.toString()
        val normalized = normalizeAccents(raw)
        val kwType = KEYWORDS[normalized]
        return if (kwType != null) {
            Token(kwType, raw, startLine, startCol)
        } else {
            Token(TokenType.IDENT, raw, startLine, startCol)
        }
    }

    /**
     * Escanea un LIT_STRING (estados C0 → 3 → 4).
     * Consume la " de apertura, lee hasta la " de cierre.
     * No se permite salto de línea dentro de la cadena.
     */
    private fun scanString(startLine: Int, startCol: Int): Token {
        val sb = StringBuilder()
        advance() // consumir la " inicial (estado 3R → 3)
        while (pos < source.length) {
            val c = peek()
            if (c == Symbol.QUOTE) {
                advance() // consumir la " de cierre (estado 3 → 4)
                return Token(TokenType.LIT_STRING, "\"$sb\"", startLine, startCol)
            }

            if (c == Symbol.INVERTED_BAR) {
                advance()
                when (peek()) {
                    Symbol.N -> sb.append(Symbol.NEW_LINE)
                    Symbol.T -> sb.append(Symbol.TAB)
                    Symbol.R -> sb.append(Symbol.RETURN)
                    Symbol.QUOTE -> sb.append(Symbol.QUOTE)
                    Symbol.INVERTED_BAR -> sb.append(Symbol.INVERTED_BAR)

                    else -> {
                        reportError(startLine, startCol, "carácter de escape no reconocido en la cadena de texto: \\${peek()}")
                        sb.append(Symbol.INVERTED_BAR).append(peek())
                    }
                }
                advance()
                continue
            }

            if (c == Symbol.NEW_LINE) {
                reportError(startLine, startCol, "cadena de texto no cerrada antes del salto de línea")
                return Token(TokenType.ERROR, "\"$sb", startLine, startCol)
            }

            sb.append(advance())
        }
        // EOF sin cerrar la cadena
        reportError(startLine, startCol, "cadena de texto no cerrada al llegar a EOF")
        return Token(TokenType.ERROR, "\"$sb", startLine, startCol)
    }

    /**
     * Escanea un LIT_ENTERO (estados C1 → 5 → 6).
     * Aplica rollback implícito en estado 6.
     */
    private fun scanInteger(startLine: Int, startCol: Int): Token {
        val sb = StringBuilder()
        while (pos < source.length && peek().isDigit()) {
            sb.append(advance())
        }
        // Estado 6: rollback (el carácter 'otro' no fue consumido)
        return Token(TokenType.LIT_ENTERO, sb.toString(), startLine, startCol)
    }

    // Primitivas del cursor
    /** Devuelve el carácter en la posición actual sin avanzar. */
    private fun peek(): Char {
        return if (pos < source.length) {
            source[pos]
        } else Symbol.EOF
    }

    /** Consume el carácter actual, actualiza línea/columna y lo retorna. */
    private fun advance(): Char {
        val c = source[pos++]
        if (c == Symbol.NEW_LINE) {
            line++
            column = firstColumn
        } else column++

        return c
    }

    /** Avanza mientras el carácter actual sea considerado espacio en blanco (estado 40 → skip). */
    private fun skipWhitespace() {
        while (pos < source.length) {
            val c = peek()
            if (Symbol.WHITESPACE_VALUES.indexOf(c ) != indexOutOfRange) advance()
            else break
        }
    }

    // Reporte de errores
    private fun reportError(l: Int, col: Int, msg: String) {
        val err = "Jaguarsh (línea $l, columna $col): $msg"
        errors.add(err)
        System.err.println(err)
    }
}