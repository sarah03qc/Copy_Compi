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
class TableDrivenScanner(private val source: String) : Scanner {
    /** Lista de errores léxicos encontrados. */
    override val errors: MutableList<String> = mutableListOf()

    // Estado interno del scanner
    private val err = -2
    private val indexOutOfRange = -1
    private val startingState = 0
    private val firstColumn = 1
    private val wsState = 40
    private val eofState = 41
    private val unknownState = 42

    private object Symbol {
        const val ACCENTED = "áäÁÄéëÉËíïÍÏóöÓÖúüÚÜñÑ"
        const val QUOTE = '"'
        const val COLON = ':'
        const val COMMA = ','
        const val SEMICOLON = ';'
        const val OPEN_PAREN = '('
        const val CLOSE_PAREN = ')'
        const val OPEN_BRACKET = '['
        const val CLOSE_BRACKET = ']'
        const val OPEN_BRACE = '{'
        const val CLOSE_BRACE = '}'
        const val PLUS = '+'
        const val MINUS = '-'
        const val ASTERISK = '*'
        const val NORMAL_BAR = '/'
        const val OPEN_ANGULAR = '<'
        const val CLOSED_ANGULAR = '>'
        const val EQUALS = '='
        const val EXCLAMATION_MARK = '!'
        const val AMPERSAND = '&'
        const val PIPE = '|'
        const val UNDERLINE = '_'
        const val TAB = '\t'
        const val NEW_LINE = '\n'
        const val RETURN = '\r'
        const val SPACE = ' '
        const val EOF = '\u0004'
        const val EOF_LEXEME = "EOF"
    }

    private var pos: Int = 0       // posición actual en source
    private var line: Int = 1      // línea actual (1-based)
    private var column: Int = firstColumn    // columna actual (1-based)
    private enum class Category {
        LETRA,      // [a-zA-Z y acentuadas y _]
        DIGITO,     // [0-9]
        COMILLA,    // "
        MENOS,      // -
        MAS,        // +
        ASTERISCO,  // *
        SLASH,      // /
        IGUAL,      // =
        EXCL,       // !
        MAYOR,      // >
        MENOR,      // <
        AMP,        // &
        PIPE,       // |
        PAR_AB,     // (
        PAR_CER,    // )
        COR_AB,     // [
        COR_CER,    // ]
        LLA_AB,     // {
        LLA_CER,    // }
        PUNTO_COMA, // ;
        COMA,       // ,
        DOS_PTS,    // :
        WS,         // espacio, tab, \r, \n
        EOF_CAT,    // fin de archivo
        OTRO        // cualquier otro carácter
    }
    private val transitionTable = arrayOf(
        /* e0  inicio      */ intArrayOf( 1,  5,  3,  7, 10, 13, 14, 15, 18, 21, 26, 24, 29, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41,err),
        /* e1  en-ident    */ intArrayOf( 1,  1,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2,  2),
        /* e2  IDENT       */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e3  en-string   */ intArrayOf( 3,  3,  4,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,  3,err,  3),
        /* e4  LIT_STRING  */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e5  en-entero   */ intArrayOf( 6,  5,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6,  6),
        /* e6  LIT_ENTERO  */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e7  vio '-'     */ intArrayOf( 9,  9,  9,  8,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9,  9),
        /* e8  OP_DEC      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e9  OP_RESTA    */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e10 vio '+'     */ intArrayOf(12, 12, 12, 12, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12),
        /* e11 OP_INC      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e12 OP_SUMA     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e13 OP_MUL      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e14 OP_DIV      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e15 vio '='     */ intArrayOf(17, 17, 17, 17, 17, 17, 17, 16, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17, 17),
        /* e16 OP_IGU      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e17 OP_ASIG     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e18 vio '!'     */ intArrayOf(20, 20, 20, 20, 20, 20, 20, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20),
        /* e19 OP_DIFE     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e20 OP_NO       */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e21 vio '>'     */ intArrayOf(23, 23, 23, 23, 23, 23, 23, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23),
        /* e22 OP_MAYOR_IG */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e23 OP_MAYOR    */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e24 vio '&'     */ intArrayOf(err,err,err,err,err,err,err,err,err,err,err, 25,err,err,err,err,err,err,err,err,err,err,err,err,err),
        /* e25 OP_Y        */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e26 vio '<'     */ intArrayOf(27, 27, 27, 27, 27, 27, 27, 28, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27),
        /* e27 OP_MENOR    */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e28 OP_MENOR_IG */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e29 vio '|'     */ intArrayOf(err,err,err,err,err,err,err,err,err,err,err,err, 30,err,err,err,err,err,err,err,err,err,err,err,err),
        /* e30 OP_O        */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e31 PAR_AB      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e32 PAR_CER     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e33 LLA_AB      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e34 LLA_CER     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e35 COR_AB      */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e36 COR_CER     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e37 FIN_SEN     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e38 SEPARAR     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e39 DOS_PTS     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e40 ESPACIO     */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e41 EOF         */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0),
        /* e99 ERROR       */ intArrayOf( 0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0)
    )
    private val action: Map<Int, Pair<TokenType, Boolean>> = mapOf(
        2 to Pair(TokenType.IDENT,             true),
        4 to Pair(TokenType.LIT_STRING,         false),
        6 to Pair(TokenType.LIT_ENTERO,         true),
        8 to Pair(TokenType.OP_DEC,             false),
        9 to Pair(TokenType.OP_RESTA,           true),
        11 to Pair(TokenType.OP_INC,             false),
        12 to Pair(TokenType.OP_SUMA,            true),
        13 to Pair(TokenType.OP_MUL,             false),
        14 to Pair(TokenType.OP_DIV,             false),
        16 to Pair(TokenType.OP_IGU,             false),
        17 to Pair(TokenType.OP_ASIG,            true),
        19 to Pair(TokenType.OP_DIFE,            false),
        20 to Pair(TokenType.OP_NO,              true),
        22 to Pair(TokenType.OP_MAYOR_IG,        false),
        23 to Pair(TokenType.OP_MAYOR,           true),
        25 to Pair(TokenType.OP_Y,               false),
        27 to Pair(TokenType.OP_MENOR,           true),
        28 to Pair(TokenType.OP_MENOR_IG,        false),
        30 to Pair(TokenType.OP_O,               false),
        31 to Pair(TokenType.PAREN_ABIERTO,      false),
        32 to Pair(TokenType.PAREN_CERRADO,      false),
        33 to Pair(TokenType.LLAVE_ABIERTA,      false),
        34 to Pair(TokenType.LLAVE_CERRADA,      false),
        35 to Pair(TokenType.CORCHETE_ABIERTO,   false),
        36 to Pair(TokenType.CORCHETE_CERRADO,   false),
        37 to Pair(TokenType.FIN_SENTENCIA,      false),
        38 to Pair(TokenType.SEPARAR,            false),
        39 to Pair(TokenType.DOS_PUNTOS,         false),
        40 to Pair(TokenType.EOF,                false), // ESPACIO → skip
        41 to Pair(TokenType.EOF,                false)
    )

    companion object {
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
            "tomela"     to TokenType.RETORNAR,
            "miau"       to TokenType.IMPRIMIR,
            "me_la_comi" to TokenType.RECIBIR,
            "jaleas"     to TokenType.BREAK,
            "dele_dele"  to TokenType.CONTINUE,
            "diay_si"    to TokenType.VERDADERO,
            "diay_no"    to TokenType.FALSO
        )

        fun normalizeAccent(s: String): String = buildString {
            for (c in s) append(when (c) {
                'á', 'Á' -> 'a'; 'é', 'É' -> 'e'
                'í', 'Í' -> 'i'; 'ó', 'Ó' -> 'o'
                'ú', 'Ú' -> 'u'
                else -> c
            })
        }
    }

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

    private fun nextToken(): Token {
        var state = 0
        val lexeme = StringBuilder()
        var tokLine  = line
        var tokCol = column

        while (true) {
            // Leer siguiente carácter (o señal de EOF)
            val c: Char   =  peek()
            val cat       = if (c != Symbol.EOF) chooseCategory(c) else Category.EOF_CAT
            val catIdx    = cat.ordinal

            // Consultar tabla
            val next = transitionTable[tableRow(state)][catIdx]

            if (next == err) {
                // Error léxico
                val msg = if (c != Symbol.EOF) "carácter no reconocido: '$c'" else "se esperaba otro caracter, en su lugar se encontró 'EOF'."
                reportError(line, column, msg)
                if (c != Symbol.EOF && peek() != Symbol.EOF) forward(lexeme)   // consumir para no ciclar
                return Token(TokenType.ERROR, lexeme.toString(), tokLine, tokCol)
            }

            // Consumir el carácter si no estamos en un estado de aceptación
            // que requiera rollback (el carácter aún no se ha consumido al
            // entrar al estado de aceptación con rollback)
            val isAccept = action.containsKey(next)

            if (!isAccept) {
                // Estado intermedio: consumir siempre
                if (c != Symbol.EOF && peek() != Symbol.EOF) forward(lexeme)
            } else {
                val (type, rollback) = action[next]!!
                if (!rollback) {
                    // Consumir el carácter que nos llevó al estado de aceptación
                    if (c != Symbol.EOF && peek() != Symbol.EOF) forward(lexeme)
                }
                // Si rollback=true NO consumimos: el carácter vuelve a ser leído
                // en el siguiente llamado a nextToken()

                state = next

                return when {
                    // ESPACIO → skip: reiniciar sin emitir token
                    type == TokenType.EOF && state == wsState -> {
                        tokLine   = line
                        tokCol = column
                        state     = startingState
                        lexeme.clear()
                        // Continuar el bucle (tail-call manual)
                        nextToken()
                    }
                    // EOF real
                    type == TokenType.EOF && state == eofState ->
                        Token(TokenType.EOF, Symbol.EOF_LEXEME, tokLine, tokCol)

                    // IDENT → verificar si es keyword
                    type == TokenType.IDENT -> {
                        val raw = lexeme.toString()
                        val kw  = KEYWORDS[normalizeAccent(raw)]
                        Token(kw ?: TokenType.IDENT, raw, tokLine, tokCol)
                    }

                    // Cualquier otro token de aceptación
                    else -> Token(type, lexeme.toString(), tokLine, tokCol)
                }
            }

            state = next

            // Si llegamos a un estado de aceptación dentro del while
            // (no debería ocurrir con la lógica de arriba, pero por seguridad)
            if (state == startingState) {
                tokLine   = line
                tokCol = column
                lexeme.clear()
            }
        }
    }

    private fun chooseCategory(c: Char): Category = when {
        c.isLetter() || c == Symbol.UNDERLINE || esAcentuada(c) -> Category.LETRA
        c.isDigit()                     -> Category.DIGITO
        c == Symbol.QUOTE               -> Category.COMILLA
        c == Symbol.MINUS               -> Category.MENOS
        c == Symbol.PLUS                -> Category.MAS
        c == Symbol.ASTERISK            -> Category.ASTERISCO
        c == Symbol.NORMAL_BAR          -> Category.SLASH
        c == Symbol.EQUALS              -> Category.IGUAL
        c == Symbol.EXCLAMATION_MARK    -> Category.EXCL
        c == Symbol.CLOSED_ANGULAR      -> Category.MAYOR
        c == Symbol.OPEN_ANGULAR        -> Category.MENOR
        c == Symbol.AMPERSAND           -> Category.AMP
        c == Symbol.PIPE                -> Category.PIPE
        c == Symbol.OPEN_PAREN          -> Category.PAR_AB
        c == Symbol.CLOSE_PAREN         -> Category.PAR_CER
        c == Symbol.OPEN_BRACKET        -> Category.COR_AB
        c == Symbol.CLOSE_BRACKET       -> Category.COR_CER
        c == Symbol.OPEN_BRACE          -> Category.LLA_AB
        c == Symbol.CLOSE_BRACE         -> Category.LLA_CER
        c == Symbol.SEMICOLON           -> Category.PUNTO_COMA
        c == Symbol.COMMA               -> Category.COMA
        c == Symbol.COLON               -> Category.DOS_PTS
        c == Symbol.SPACE || c == Symbol.TAB || c == Symbol.RETURN || c == Symbol.NEW_LINE -> Category.WS
        c == Symbol.EOF                 -> Category.EOF_CAT
        else                            -> Category.OTRO
    }

    private fun esAcentuada(c: Char): Boolean = c in
            Symbol.ACCENTED

    private fun tableRow(state: Int): Int = when (state) {
        in startingState..eofState -> state
        err -> unknownState
        else -> unknownState
    }

    private fun peek(): Char {
        return if (pos < source.length) {
            source[pos]
        } else Symbol.EOF
    }

    private fun forward(lexeme: StringBuilder): Char {
        val c = source[pos++]
        lexeme.append(c)
        if (c == Symbol.NEW_LINE) {
            line++
            column = firstColumn
        } else column++

        return c
    }

    private fun reportError(l: Int, col: Int, msg: String) {
        val err = "Jaguarsh (línea $l, columna $col): $msg"
        errors.add(err)
        System.err.println(err)
    }
}