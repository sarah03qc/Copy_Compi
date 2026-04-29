package edu.ic5701

import edu.ic5701.parser.ParserImpl
import edu.ic5701.scanner.ScannerImpl
import org.junit.Assert
import org.junit.Test

class ParserImplTest {

    // utilidad: escanea y parsea, retorna el parser para inspeccionar errores
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
    // 3. declaraciones de variable y constante
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
    // 4. sentencia de retorno
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

    // =========================================================================
    // 5. condicional
    // =========================================================================

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

    // =========================================================================
    // 6. ciclo
    // =========================================================================

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
    // 7. sentencias de expresion
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
    // 8. impresion
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
    // 9. archivos de muestra completos
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

    // =========================================================================
    // 10. errores sintacticos esperados
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