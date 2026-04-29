package edu.ic5701

import edu.ic5701.parser.ParserImpl
import edu.ic5701.scanner.ScannerImpl
import org.junit.Assert
import org.junit.Test

class ParserImplTest {

    private fun parsear(fuente: String): ParserImpl {
        val tokens = ScannerImpl(fuente).scanAll()
        val parser = ParserImpl(tokens)
        parser.parse()
        return parser
    }

    private fun sinErrores(fuente: String) {
        val parser = parsear(fuente)
        Assert.assertTrue(
            "se esperaba que parseara sin errores pero hubo: ${parser.errors}",
            parser.errors.isEmpty()
        )
    }

    private fun conErrores(fuente: String) {
        val parser = parsear(fuente)
        Assert.assertTrue(
            "se esperaba al menos un error sintactico pero no hubo ninguno",
            parser.errors.isNotEmpty()
        )
    }

    // =========================================================================
    // 1. funcion minima
    // =========================================================================

    @Test
    fun funcion_vacia_con_retorno_vacio() {
        sinErrores("""
            chunche ni_papa chante() {
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun funcion_con_tipo_entero() {
        sinErrores("""
            chunche colones doble(colones n) {
                tomela n;
            } : colones
        """.trimIndent())
    }

    @Test
    fun funcion_con_multiples_params() {
        sinErrores("""
            chunche colones suma(colones a, colones b) {
                tomela a;
            } : colones
        """.trimIndent())
    }

    @Test
    fun funcion_con_tipo_arreglo_en_param() {
        sinErrores("""
            chunche ni_papa procesar(fila_india(colones) arr, colones n) {
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun funcion_con_arreglo_anidado_en_param() {
        sinErrores("""
            chunche ni_papa procesar(fila_india(fila_india(colones)) matriz) {
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 2. multiples funciones
    // =========================================================================

    @Test
    fun dos_funciones_seguidas() {
        sinErrores("""
            chunche colones uno() {
                tomela 1;
            } : colones
            chunche colones dos() {
                tomela 2;
            } : colones
        """.trimIndent())
    }

    // =========================================================================
    // 3. declaraciones
    // =========================================================================

    @Test
    fun decl_variable_entera() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones x = 0;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun decl_constante() {
        sinErrores("""
            chunche ni_papa chante() {
                de_fijo colones MAX = 10;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun decl_variable_booleana() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay activo = diay_si;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun decl_variable_cadena() {
        sinErrores("""
            chunche ni_papa chante() {
                vara labia mensaje = "hola";
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 4. sentencias de control
    // =========================================================================

    @Test
    fun retorno_literal_entero() {
        sinErrores("""
            chunche colones uno() {
                tomela 1;
            } : colones
        """.trimIndent())
    }

    @Test
    fun retorno_ni_papa() {
        sinErrores("""
            chunche ni_papa chante() {
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun condicional_sin_sino() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones x = 0;
                mae (x == 0) {
                    tomela ni_papa;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun condicional_con_sino() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones x = 0;
                mae (x == 0) {
                    tomela ni_papa;
                } tons {
                    tomela ni_papa;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun ciclo_basico() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones i = 0;
                bretee (i < 10) {
                    i++;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun ciclo_con_break() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones i = 0;
                bretee (i < 10) {
                    jaleas;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun ciclo_con_continue() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones i = 0;
                bretee (i < 10) {
                    dele_dele;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 5. sentencias de expresion
    // =========================================================================

    @Test
    fun asignacion_simple() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones x = 0;
                x = 5;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun incremento_como_sentencia() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones i = 0;
                i++;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun decremento_como_sentencia() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones i = 0;
                i--;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun llamada_a_funcion_como_sentencia() {
        sinErrores("""
            chunche ni_papa ayuda() {
                tomela ni_papa;
            } : ni_papa
            chunche ni_papa chante() {
                ayuda();
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun asignacion_a_elemento_de_arreglo() {
        sinErrores("""
            chunche ni_papa chante() {
                vara fila_india(colones) arr = [1, 2, 3];
                arr[0] = 99;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 6. impresion
    // =========================================================================

    @Test
    fun miau_sin_args() {
        sinErrores("""
            chunche ni_papa chante() {
                miau();
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun miau_con_un_arg() {
        sinErrores("""
            chunche ni_papa chante() {
                miau("hola");
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun miau_con_multiples_args() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones x = 1;
                miau("valor: ", x);
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 7. expresiones por nivel de precedencia
    // =========================================================================

    @Test
    fun expresion_or() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = diay_si || diay_no;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_and() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = diay_si && diay_no;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_igualdad() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = 1 == 1;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_diferente() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = 1 != 2;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_relacional() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = 1 < 2;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_aritmetica() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones r = 1 + 2 * 3 - 4 / 2;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_agrupada_con_parentesis() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones r = (1 + 2) * 3;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_negacion_logica() {
        sinErrores("""
            chunche ni_papa chante() {
                vara diay r = !diay_si;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_negacion_aritmetica() {
        sinErrores("""
            chunche ni_papa chante() {
                vara colones r = -5;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_llamada_en_expresion() {
        sinErrores("""
            chunche colones doble(colones n) {
                tomela n;
            } : colones
            chunche ni_papa chante() {
                vara colones r = doble(5) + 1;
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_acceso_array_en_expresion() {
        sinErrores("""
            chunche ni_papa chante() {
                vara fila_india(colones) arr = [1, 2, 3];
                vara colones r = arr[0] + arr[1];
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_array_literal() {
        sinErrores("""
            chunche ni_papa chante() {
                vara fila_india(colones) arr = [1, 2, 3];
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun expresion_me_la_comi() {
        sinErrores("""
            chunche ni_papa chante() {
                vara labia entrada = me_la_comi();
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 8. archivos de muestra completos
    // =========================================================================

    @Test
    fun muestra_factorial() {
        sinErrores("""
            chunche colones factorial(colones n) {
                mae (n == 0) {
                    tomela 1;
                } tons {
                    tomela n * factorial(n - 1);
                }
            } : colones
            chunche ni_papa chante() {
                vara colones resultado = factorial(10);
                miau("factorial(10) = ", resultado);
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun muestra_burbuja() {
        sinErrores("""
            chunche ni_papa burbuja(fila_india(colones) arr, colones n) {
                vara colones i = 0;
                bretee (i < n) {
                    vara colones j = 0;
                    bretee (j < n - i - 1) {
                        mae (arr[j] > arr[j + 1]) {
                            vara colones temp = arr[j];
                            arr[j] = arr[j + 1];
                            arr[j + 1] = temp;
                        }
                        j++;
                    }
                    i++;
                }
                tomela ni_papa;
            } : ni_papa
            chunche ni_papa chante() {
                vara fila_india(colones) datos = [64, 34, 25, 12, 22];
                burbuja(datos, 5);
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun muestra_palindrome() {
        sinErrores("""
            chunche diay esPalindromo(fila_india(labia) chars, colones largo) {
                vara colones izq = 0;
                vara colones der = largo - 1;
                vara diay resultado = diay_si;
                bretee (izq < der) {
                    mae (chars[izq] != chars[der]) {
                        resultado = diay_no;
                        jaleas;
                    }
                    izq++;
                    der--;
                }
                tomela resultado;
            } : diay
            chunche ni_papa chante() {
                vara fila_india(labia) palabra = ["a", "n", "i", "l", "i", "n", "a"];
                vara diay res = esPalindromo(palabra, 7);
                mae (res == diay_si) {
                    miau("la palabra es palindromo");
                } tons {
                    miau("la palabra NO es palindromo");
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    // =========================================================================
    // 9. errores sintacticos esperados
    // =========================================================================

    @Test
    fun error_falta_chunche() {
        conErrores("""
            colones factorial(colones n) {
                tomela 1;
            } : colones
        """.trimIndent())
    }

    @Test
    fun error_falta_dos_puntos_al_final() {
        conErrores("""
            chunche ni_papa chante() {
                tomela ni_papa;
            } ni_papa
        """.trimIndent())
    }

    @Test
    fun error_falta_fin_sentencia() {
        conErrores("""
            chunche ni_papa chante() {
                vara colones x = 0
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }

    @Test
    fun error_falta_paren_cierre_en_condicion() {
        conErrores("""
            chunche ni_papa chante() {
                mae (diay_si {
                    tomela ni_papa;
                }
                tomela ni_papa;
            } : ni_papa
        """.trimIndent())
    }
}