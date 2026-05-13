package edu.ic5701

import edu.ic5701.ast.nodes.*
import edu.ic5701.sem.NameResolver
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests del analizador semántico de resolución de nombres (T9b).
 *
 * Cada test construye un AST mínimo directamente (sin pasar por el parser)
 * y verifica que NameResolver detecte o acepte correctamente el programa.
 */
class NameResolverTest {

    private val N = 0

    // -----------------------------------------------------------------------
    // Helpers para construir ASTs de prueba
    // -----------------------------------------------------------------------

    /** Función de entrada mínima válida: chante(): ni_papa { } */
    private fun chanteVacia(): FuncionDecl = FuncionDecl(
        inicio = N, fin = N,
        nombre = Identificador(N, N, "chante"),
        esEntrada = true,
        retorno = TipoNiPapa(N, N),
        params = emptyList(),
        cuerpo = Bloque(N, N, emptyList())
    )

    /** Programa válido con solo chante vacía */
    private fun programaMinimo(): Programa =
        Programa(N, N, listOf(chanteVacia()))

    // -----------------------------------------------------------------------
    // Programa mínimo válido
    // -----------------------------------------------------------------------

    @Test
    fun `programa con chante vacia es valido`() {
        val resolver = NameResolver()
        resolver.visitPrograma(programaMinimo())
        assertFalse(resolver.tieneErrores(), resolver.errores.joinToString())
    }

    // -----------------------------------------------------------------------
    // Verificación de punto de entrada
    // -----------------------------------------------------------------------

    @Test
    fun `programa sin chante reporta error`() {
        val resolver = NameResolver()
        val prog = Programa(
            N, N,
            listOf(
                FuncionDecl(N, N, Identificador(N, N, "otra"), false,
                    TipoNiPapa(N, N), emptyList(), Bloque(N, N, emptyList()))
            )
        )
        resolver.visitPrograma(prog)
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("chante") })
    }

    @Test
    fun `chante con parametros reporta error`() {
        val resolver = NameResolver()
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N),
            listOf(Parametro(N, N, TipoColones(N, N), Identificador(N, N, "x"))),
            Bloque(N, N, emptyList())
        )
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("parámetros") || it.contains("chante") })
    }

    @Test
    fun `chante con retorno distinto a ni_papa reporta error`() {
        val resolver = NameResolver()
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoColones(N, N), emptyList(), Bloque(N, N, emptyList())
        )
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("ni_papa") || it.contains("chante") })
    }

    // -----------------------------------------------------------------------
    // Declaración de variables
    // -----------------------------------------------------------------------

    @Test
    fun `variable declarada es accesible en el mismo scope`() {
        val declX = VariableDecl(N, N, Identificador(N, N, "x"), TipoColones(N, N), LiteralEntero(N, N, 5))
        val usoX  = AsignacionSimple(N, N, Identificador(N, N, "x"), LiteralEntero(N, N, 10))

        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(),
            Bloque(N, N, listOf(declX, usoX))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertFalse(resolver.tieneErrores(), resolver.errores.joinToString())
    }

    @Test
    fun `variable no declarada reporta error`() {
        val uso = AsignacionSimple(N, N, Identificador(N, N, "noExiste"), LiteralEntero(N, N, 1))
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(uso))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("noExiste") })
    }

    @Test
    fun `variable duplicada en mismo scope reporta error`() {
        val decl1 = VariableDecl(N, N, Identificador(N, N, "x"), TipoColones(N, N), LiteralEntero(N, N, 1))
        val decl2 = VariableDecl(N, N, Identificador(N, N, "x"), TipoColones(N, N), LiteralEntero(N, N, 2))
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(decl1, decl2))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("'x'") || it.contains("x") })
    }

    // -----------------------------------------------------------------------
    // Constantes
    // -----------------------------------------------------------------------

    @Test
    fun `reasignar constante reporta error`() {
        val declC = ConstanteDecl(N, N, Identificador(N, N, "MAX"), TipoColones(N, N), LiteralEntero(N, N, 100))
        val asig  = AsignacionSimple(N, N, Identificador(N, N, "MAX"), LiteralEntero(N, N, 200))
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(declC, asig))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("MAX") || it.contains("constante") })
    }

    // -----------------------------------------------------------------------
    // Scoping y shadowing
    // -----------------------------------------------------------------------

    @Test
    fun `variable en scope externo es visible en scope interno`() {
        val declX  = VariableDecl(N, N, Identificador(N, N, "x"), TipoColones(N, N), LiteralEntero(N, N, 0))
        val usoX   = AsignacionSimple(N, N, Identificador(N, N, "x"), LiteralEntero(N, N, 1))
        val bloque = Bloque(N, N, listOf(usoX))
        val cond   = Condicional(N, N, LiteralBooleano(N, N, true), bloque, null)
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(declX, cond))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertFalse(resolver.tieneErrores(), resolver.errores.joinToString())
    }

    @Test
    fun `variable declarada en bloque interno no es visible fuera`() {
        // Declara y usa x dentro de un bloque condicional
        val declX     = VariableDecl(N, N, Identificador(N, N, "x"), TipoColones(N, N), LiteralEntero(N, N, 0))
        val bloqueInt = Bloque(N, N, listOf(declX))
        val cond      = Condicional(N, N, LiteralBooleano(N, N, true), bloqueInt, null)
        // Intenta usar x fuera del bloque
        val usoXFuera = AsignacionSimple(N, N, Identificador(N, N, "x"), LiteralEntero(N, N, 5))
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(cond, usoXFuera))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("'x'") || it.contains("x") })
    }

    // -----------------------------------------------------------------------
    // Parámetros
    // -----------------------------------------------------------------------

    @Test
    fun `parametro es accesible dentro del cuerpo de la funcion`() {
        val param = Parametro(N, N, TipoColones(N, N), Identificador(N, N, "n"))
        val retorno = Retorno(N, N, Identificador(N, N, "n"))
        val func = FuncionDecl(
            N, N, Identificador(N, N, "doble"), false,
            TipoColones(N, N), listOf(param), Bloque(N, N, listOf(retorno))
        )
        val chante = chanteVacia()
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(func, chante)))
        assertFalse(resolver.tieneErrores(), resolver.errores.joinToString())
    }

    @Test
    fun `parametro duplicado reporta error`() {
        val param1 = Parametro(N, N, TipoColones(N, N), Identificador(N, N, "n"))
        val param2 = Parametro(N, N, TipoColones(N, N), Identificador(N, N, "n"))
        val func = FuncionDecl(
            N, N, Identificador(N, N, "f"), false,
            TipoColones(N, N), listOf(param1, param2), Bloque(N, N, emptyList())
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(func, chanteVacia())))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("'n'") || it.contains("n") })
    }

    // -----------------------------------------------------------------------
    // Llamadas a función
    // -----------------------------------------------------------------------

    @Test
    fun `llamada a funcion declarada es valida`() {
        val suma = FuncionDecl(
            N, N, Identificador(N, N, "suma"), false,
            TipoColones(N, N),
            listOf(
                Parametro(N, N, TipoColones(N, N), Identificador(N, N, "a")),
                Parametro(N, N, TipoColones(N, N), Identificador(N, N, "b"))
            ),
            Bloque(N, N, listOf(
                Retorno(N, N, OperacionSuma(N, N, Identificador(N, N, "a"), Identificador(N, N, "b")))
            ))
        )
        val llamada = SentenciaExpr(
            N, N,
            LlamadaFuncion(N, N, Identificador(N, N, "suma"),
                listOf(LiteralEntero(N, N, 1), LiteralEntero(N, N, 2)))
        )
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(llamada))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(suma, chante)))
        assertFalse(resolver.tieneErrores(), resolver.errores.joinToString())
    }

    @Test
    fun `llamada a funcion no declarada reporta error`() {
        val llamada = SentenciaExpr(
            N, N,
            LlamadaFuncion(N, N, Identificador(N, N, "noExiste"), emptyList())
        )
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(llamada))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("noExiste") })
    }

    @Test
    fun `llamada con cantidad incorrecta de argumentos reporta error`() {
        val suma = FuncionDecl(
            N, N, Identificador(N, N, "suma"), false,
            TipoColones(N, N),
            listOf(
                Parametro(N, N, TipoColones(N, N), Identificador(N, N, "a")),
                Parametro(N, N, TipoColones(N, N), Identificador(N, N, "b"))
            ),
            Bloque(N, N, emptyList())
        )
        // Llama con 1 argumento en vez de 2
        val llamada = SentenciaExpr(
            N, N,
            LlamadaFuncion(N, N, Identificador(N, N, "suma"),
                listOf(LiteralEntero(N, N, 1)))
        )
        val chante = FuncionDecl(
            N, N, Identificador(N, N, "chante"), true,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, listOf(llamada))
        )
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(suma, chante)))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("suma") })
    }

    // -----------------------------------------------------------------------
    // Funciones duplicadas
    // -----------------------------------------------------------------------

    @Test
    fun `funcion duplicada reporta error`() {
        val f1 = FuncionDecl(N, N, Identificador(N, N, "f"), false,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, emptyList()))
        val f2 = FuncionDecl(N, N, Identificador(N, N, "f"), false,
            TipoNiPapa(N, N), emptyList(), Bloque(N, N, emptyList()))
        val resolver = NameResolver()
        resolver.visitPrograma(Programa(N, N, listOf(f1, f2, chanteVacia())))
        assertTrue(resolver.tieneErrores())
        assertTrue(resolver.errores.any { it.contains("'f'") || it.contains("f") })
    }
}
