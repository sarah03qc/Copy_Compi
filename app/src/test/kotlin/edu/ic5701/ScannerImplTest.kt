package edu.ic5701

import edu.ic5701.scanner.ScannerImpl
import edu.ic5701.tokens.Token
import edu.ic5701.tokens.TokenType
import org.junit.Assert
import org.junit.Test

class ScannerImplTest {

    // -------------------------------------------------------------------------
    // Utilidad: escanea y retorna solo los tipos de token (sin EOF)
    // -------------------------------------------------------------------------
    private fun tipos(source: String): List<TokenType> {
        val scanner = ScannerImpl(source)
        return scanner.scanAll().map { it.type }.dropLast(1)
    }

    private fun tokens(source: String): List<Token> {
        val scanner = ScannerImpl(source)
        return scanner.scanAll().dropLast(1) // quita EOF
    }

    // =========================================================================
    // 1. Keywords
    // =========================================================================

    @Test
    fun keywords_todas() {
        val input = """
            chunche vara de_fijo colones diay labia ni_papa fila_india
            mae tons bretee tomela miau me_la_comi jaleas dele_dele
            diay_si diay_no
        """.trimIndent()

        val esperado = listOf(
            TokenType.FUNCION,
            TokenType.VARIABLE,
            TokenType.CONSTANTE,
            TokenType.ENTERO,
            TokenType.BOOLEANO,
            TokenType.CADENA,
            TokenType.VACIO,
            TokenType.ARREGLO,
            TokenType.CONDICIONAL,
            TokenType.SINO,
            TokenType.CICLO_INDEF,
            TokenType.RETORNAR,
            TokenType.IMPRIMIR,
            TokenType.RECIBIR,
            TokenType.BREAK,
            TokenType.CONTINUE,
            TokenType.VERDADERO,
            TokenType.FALSO
        )

        Assert.assertEquals(esperado, tipos(input))
    }

    @Test
    fun keyword_tomela_con_tilde() {
        Assert.assertEquals(listOf(TokenType.RETORNAR), tipos("tómela"))
    }

    @Test
    fun keyword_recibir_con_tilde() {
        Assert.assertEquals(listOf(TokenType.RECIBIR), tipos("me_la_comí"))
    }

    @Test
    fun keyword_verdadero_con_tilde() {
        Assert.assertEquals(listOf(TokenType.VERDADERO), tipos("diay_sí"))
    }

    // =========================================================================
    // 2. Identificadores
    // =========================================================================

    @Test
    fun ident_simple() {
        val toks = tokens("factorial")
        Assert.assertEquals(1, toks.size)
        Assert.assertEquals(TokenType.IDENT, toks[0].type)
        Assert.assertEquals("factorial", toks[0].lexeme)
    }

    @Test
    fun ident_con_digitos_y_guion() {
        Assert.assertEquals(listOf(TokenType.IDENT), tipos("mi_variable1"))
    }

    @Test
    fun ident_acentuado() {
        Assert.assertEquals(listOf(TokenType.IDENT), tipos("índice"))
    }

    @Test
    fun ident_mayusculas() {
        val toks = tokens("MAX")
        Assert.assertEquals(TokenType.IDENT, toks[0].type)
        Assert.assertEquals("MAX", toks[0].lexeme)
    }

    @Test
    fun ident_no_empieza_con_digito() {
        // "1abc" se escanea como LIT_ENTERO("1") + IDENT("abc")
        Assert.assertEquals(listOf(TokenType.LIT_ENTERO, TokenType.IDENT), tipos("1abc"))
    }

    @Test
    fun ident_chante() {
        Assert.assertEquals(listOf(TokenType.IDENT), tipos("chante"))
    }

    // =========================================================================
    // 3. Literales enteros
    // =========================================================================

    @Test
    fun lit_entero_un_digito() {
        val toks = tokens("0")
        Assert.assertEquals(TokenType.LIT_ENTERO, toks[0].type)
        Assert.assertEquals("0", toks[0].lexeme)
    }

    @Test
    fun lit_entero_multidigito() {
        val toks = tokens("1234")
        Assert.assertEquals(TokenType.LIT_ENTERO, toks[0].type)
        Assert.assertEquals("1234", toks[0].lexeme)
    }

    @Test
    fun lit_enteros_multiples() {
        Assert.assertEquals(listOf(TokenType.LIT_ENTERO, TokenType.LIT_ENTERO, TokenType.LIT_ENTERO), tipos("1 22 333"))
    }

    // =========================================================================
    // 4. Literales string
    // =========================================================================

    @Test
    fun lit_string_vacio() {
        val toks = tokens("\"\"")
        Assert.assertEquals(TokenType.LIT_STRING, toks[0].type)
        Assert.assertEquals("\"\"", toks[0].lexeme)
    }

    @Test
    fun lit_string_con_contenido() {
        val toks = tokens("\"hola mundo\"")
        Assert.assertEquals(TokenType.LIT_STRING, toks[0].type)
        Assert.assertEquals("\"hola mundo\"", toks[0].lexeme)
    }

    @Test
    fun lit_string_caracteres_especiales() {
        val toks = tokens("\"5+3=b\"")
        Assert.assertEquals(TokenType.LIT_STRING, toks[0].type)
    }

    @Test
    fun lit_string_sin_cerrar() {
        val scanner = ScannerImpl("\"cadena sin cierre")
        scanner.scanAll()
        Assert.assertTrue("Debe reportar error léxico", scanner.errors.isNotEmpty())
    }

    // =========================================================================
    // 5. Operadores aritméticos
    // =========================================================================

    @Test
    fun operadores_aritmeticos() {
        Assert.assertEquals(
            listOf(TokenType.OP_SUMA, TokenType.OP_RESTA, TokenType.OP_MUL, TokenType.OP_DIV),
            tipos("+ - * /")
        )
    }

    @Test
    fun op_incremento() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_INC), tipos("x++"))
    }

    @Test
    fun op_decremento() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_DEC), tipos("y--"))
    }

    @Test
    fun un_suma_no_es_incremento() {
        Assert.assertEquals(listOf(TokenType.OP_SUMA, TokenType.OP_SUMA), tipos("+ +"))
    }

    // =========================================================================
    // 6. Operadores relacionales y de asignación
    // =========================================================================

    @Test
    fun op_asignacion() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_ASIG, TokenType.LIT_ENTERO), tipos("x = 5"))
    }

    @Test
    fun op_igualdad() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_IGU, TokenType.LIT_ENTERO), tipos("x == 0"))
    }

    @Test
    fun op_diferente() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_DIFE, TokenType.LIT_ENTERO), tipos("x != 0"))
    }

    @Test
    fun op_mayor() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_MAYOR, TokenType.IDENT), tipos("a > b"))
    }

    @Test
    fun op_mayor_ig() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_MAYOR_IG, TokenType.IDENT), tipos("a >= b"))
    }

    @Test
    fun op_menor() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_MENOR, TokenType.IDENT), tipos("a < b"))
    }

    @Test
    fun op_menor_ig() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_MENOR_IG, TokenType.IDENT), tipos("a <= b"))
    }

    // =========================================================================
    // 7. Operadores lógicos
    // =========================================================================

    @Test
    fun op_y() {
        Assert.assertEquals(listOf(TokenType.VERDADERO, TokenType.OP_Y, TokenType.FALSO), tipos("diay_si && diay_no"))
    }

    @Test
    fun op_o() {
        Assert.assertEquals(listOf(TokenType.VERDADERO, TokenType.OP_O, TokenType.FALSO), tipos("diay_si || diay_no"))
    }

    @Test
    fun op_no() {
        Assert.assertEquals(listOf(TokenType.OP_NO, TokenType.IDENT), tipos("!condicion"))
    }

    @Test
    fun and_incompleto_es_error() {
        val scanner = ScannerImpl("&")
        scanner.scanAll()
        Assert.assertTrue("& solo debe reportar error léxico", scanner.errors.isNotEmpty())
    }

    @Test
    fun or_incompleto_es_error() {
        val scanner = ScannerImpl("|")
        scanner.scanAll()
        Assert.assertTrue("| solo debe reportar error léxico", scanner.errors.isNotEmpty())
    }

    // =========================================================================
    // 8. Delimitadores
    // =========================================================================

    @Test
    fun parentesis() {
        Assert.assertEquals(listOf(TokenType.PAREN_ABIERTO, TokenType.PAREN_CERRADO), tipos("()"))
    }

    @Test
    fun corchetes_bloque() {
        Assert.assertEquals(listOf(TokenType.CORCHETE_ABIERTO, TokenType.CORCHETE_CERRADO), tipos("{}"))
    }

    @Test
    fun llaves_arreglo() {
        Assert.assertEquals(listOf(TokenType.LLAVE_ABIERTA, TokenType.LLAVE_CERRADA), tipos("[]"))
    }

    @Test
    fun fin_sentencia() {
        Assert.assertEquals(listOf(TokenType.FIN_SENTENCIA), tipos(";"))
    }

    @Test
    fun separar() {
        Assert.assertEquals(listOf(TokenType.SEPARAR), tipos(","))
    }

    @Test
    fun dos_puntos() {
        Assert.assertEquals(listOf(TokenType.DOS_PUNTOS), tipos(":"))
    }

    // =========================================================================
    // 9. Espacios en blanco y saltos de línea
    // =========================================================================

    @Test
    fun espacios_ignorados() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_ASIG, TokenType.LIT_ENTERO), tipos("  x   =   5  "))
    }

    @Test
    fun saltos_de_linea_ignorados() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_ASIG, TokenType.LIT_ENTERO), tipos("x\n=\n5"))
    }

    @Test
    fun tabs_ignorados() {
        Assert.assertEquals(listOf(TokenType.IDENT, TokenType.OP_ASIG, TokenType.LIT_ENTERO), tipos("x\t=\t5"))
    }

    // =========================================================================
    // 10. Metadatos de posición (línea y columna)
    // =========================================================================

    @Test
    fun posicion_primer_token() {
        val toks = tokens("chunche")
        Assert.assertEquals(1, toks[0].line)
        Assert.assertEquals(1, toks[0].column)
    }

    @Test
    fun posicion_columna() {
        val toks = tokens("x = 5")
        // x en col 1, = en col 3, 5 en col 5
        Assert.assertEquals(1, toks[0].column)  // x
        Assert.assertEquals(3, toks[1].column)  // =
        Assert.assertEquals(5, toks[2].column)  // 5
    }

    @Test
    fun posicion_linea() {
        val toks = tokens("x\ny")
        Assert.assertEquals(1, toks[0].line) // x
        Assert.assertEquals(2, toks[1].line) // y
    }

    // =========================================================================
    // 11. EOF
    // =========================================================================

    @Test
    fun entrada_vacia_produce_EOF() {
        val toks = ScannerImpl("").scanAll()
        Assert.assertEquals(1, toks.size)
        Assert.assertEquals(TokenType.EOF, toks[0].type)
    }

    @Test
    fun eof_es_ultimo() {
        val toks = ScannerImpl("vara colones x = 1;").scanAll()
        Assert.assertEquals(TokenType.EOF, toks.last().type)
    }
    @Test
    fun archivo_vacio() {
        val toks = ScannerImpl("").scanAll()
        Assert.assertEquals(TokenType.EOF, toks.last().type)
    }
    // =========================================================================
    // 12. Caracteres no reconocidos
    // =========================================================================

    @Test
    fun caracter_no_reconocido() {
        val scanner = ScannerImpl("@")
        val toks = scanner.scanAll()
        Assert.assertTrue(toks.any { it.type == TokenType.ERROR })
        Assert.assertTrue(scanner.errors.isNotEmpty())
    }

    @Test
    fun error_contiene_linea() {
        val scanner = ScannerImpl("@")
        scanner.scanAll()
        Assert.assertTrue(scanner.errors[0].contains("línea 1"))
    }

    // =========================================================================
    // 13. Programas de muestra completos
    // =========================================================================

    @Test
    fun programa_factorial() {
        val fuente = """
            chunche colones factorial(colones n) {
                mae (n == 0) {
                    tomela 1;
                } tons {
                    tomela n * factorial(n - 1);
                }
            }
        """.trimIndent()

        val scanner = ScannerImpl(fuente)
        val toks = scanner.scanAll()

        Assert.assertTrue("No debe haber errores léxicos", scanner.errors.isEmpty())
        Assert.assertEquals(
            listOf(
                TokenType.FUNCION,
                TokenType.ENTERO,
                TokenType.IDENT,
                TokenType.PAREN_ABIERTO,
                TokenType.ENTERO,
                TokenType.IDENT,
                TokenType.PAREN_CERRADO,
                TokenType.CORCHETE_ABIERTO,
                TokenType.CONDICIONAL,
                TokenType.PAREN_ABIERTO,
                TokenType.IDENT,
                TokenType.OP_IGU,
                TokenType.LIT_ENTERO,
                TokenType.PAREN_CERRADO,
                TokenType.CORCHETE_ABIERTO,
                TokenType.RETORNAR,
                TokenType.LIT_ENTERO,
                TokenType.FIN_SENTENCIA,
                TokenType.CORCHETE_CERRADO,
                TokenType.SINO,
                TokenType.CORCHETE_ABIERTO,
                TokenType.RETORNAR,
                TokenType.IDENT,
                TokenType.OP_MUL,
                TokenType.IDENT,
                TokenType.PAREN_ABIERTO,
                TokenType.IDENT,
                TokenType.OP_RESTA,
                TokenType.LIT_ENTERO,
                TokenType.PAREN_CERRADO,
                TokenType.FIN_SENTENCIA,
                TokenType.CORCHETE_CERRADO,
                TokenType.CORCHETE_CERRADO,
                TokenType.EOF
            ),
            toks.map { it.type }
        )
    }

    @Test
    fun programa_burbuja() {
        val fuente = """
            chunche ni_papa burbuja(fila_india(colones) arr, colones n) {
                vara colones i = 0;
                bretee (i < n) {
                    vara colones j = 0;
                    bretee (j < n - 1) {
                        mae (arr[j] > arr[j + 1]) {
                            vara colones tmp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = tmp;
                        }
                        j++;
                    }
                    i++;
                }
                tomela ni_papa;
            }
        """.trimIndent()

        val scanner = ScannerImpl(fuente)
        scanner.scanAll()
        Assert.assertTrue("No debe haber errores léxicos en burbuja", scanner.errors.isEmpty())
    }

    @Test
    fun declaracion_constante() {
        val scanner = ScannerImpl("de_fijo colones MAX = 10;")
        val toks = scanner.scanAll()

        Assert.assertTrue(scanner.errors.isEmpty())
        Assert.assertEquals(
            listOf(
                TokenType.CONSTANTE,
                TokenType.ENTERO,
                TokenType.IDENT,
                TokenType.OP_ASIG,
                TokenType.LIT_ENTERO,
                TokenType.FIN_SENTENCIA,
                TokenType.EOF
            ),
            toks.map { it.type }
        )
    }

    @Test
    fun arreglo_bidimensional() {
        val fuente = "vara fila_india(fila_india(colones)) matriz = [[1, 2], [3, 4]];"
        val scanner = ScannerImpl(fuente)
        scanner.scanAll()
        Assert.assertTrue("No debe haber errores léxicos en arreglo 2D", scanner.errors.isEmpty())
    }

    @Test
    fun sent_miau_multiples_args() {
        val scanner = ScannerImpl("miau(\"Resultado: \", resultado);")
        val toks = scanner.scanAll()
        Assert.assertTrue(scanner.errors.isEmpty())
        Assert.assertEquals(TokenType.IMPRIMIR, toks[0].type)
    }

    @Test
    fun sent_me_la_comi() {
        val scanner = ScannerImpl("vara labia entrada = me_la_comi();")
        val toks = scanner.scanAll()
        Assert.assertTrue(scanner.errors.isEmpty())
        Assert.assertTrue(toks.any { it.type == TokenType.RECIBIR })
    }
}