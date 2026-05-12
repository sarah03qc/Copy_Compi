package edu.ic5701

import edu.ic5701.ast.nodes.Bloque
import edu.ic5701.ast.nodes.Identificador
import edu.ic5701.ast.nodes.Parametro
import edu.ic5701.ast.nodes.TipoDato
import edu.ic5701.ast.nodes.TipoColones
import edu.ic5701.ast.nodes.TipoDiay
import edu.ic5701.ast.nodes.TipoLabia
import edu.ic5701.ast.nodes.TipoNiPapa
import edu.ic5701.symboltable.Categoria
import edu.ic5701.symboltable.ConstanteEntry
import edu.ic5701.symboltable.FuncionEntry
import edu.ic5701.symboltable.ParametroEntry
import edu.ic5701.symboltable.SemanticError
import edu.ic5701.symboltable.StackSymbolTable
import edu.ic5701.symboltable.VariableEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StackSymbolTableTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private val N = 0

    private fun tabla() = StackSymbolTable()

    private fun varEntry(nombre: String, tipo: TipoDato = TipoColones(N, N)) =
        VariableEntry(nombre = nombre, tipoDato = tipo)

    private fun constEntry(nombre: String, tipo: TipoDato = TipoColones(N, N)) =
        ConstanteEntry(nombre = nombre, tipoDato = tipo)

    private fun paramEntry(nombre: String, tipo: TipoDato = TipoColones(N, N)) =
        ParametroEntry(nombre = nombre, tipoDato = tipo)

    private fun funcEntry(nombre: String) = FuncionEntry(
        nombre = nombre,
        retorno = TipoNiPapa(0,0),
        params = emptyList(),
        cuerpo = Bloque(N, N, emptyList()),
        esEntrada = false
    )

    // -----------------------------------------------------------------------
    // newScope / pop / depth
    // -----------------------------------------------------------------------

    @Test
    fun `tabla inicia sin scopes`() {
        val st = tabla()
        assertEquals(0, st.depth())
    }

    @Test
    fun `newScope incrementa depth`() {
        val st = tabla()
        st.newScope()
        assertEquals(1, st.depth())
        st.newScope()
        assertEquals(2, st.depth())
    }

    @Test
    fun `pop decrementa depth`() {
        val st = tabla()
        st.newScope()
        st.newScope()
        st.pop()
        assertEquals(1, st.depth())
    }

    @Test
    fun `pop en pila vacia lanza IllegalStateException`() {
        val st = tabla()
        assertFailsWith<IllegalStateException> { st.pop() }
    }

    @Test
    fun `newScope y pop simetricos`() {
        val st = tabla()
        repeat(5) { st.newScope() }
        repeat(5) { st.pop() }
        assertEquals(0, st.depth())
    }

    // -----------------------------------------------------------------------
    // set — registro básico
    // -----------------------------------------------------------------------

    @Test
    fun `set registra variable en scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("x", varEntry("x"))
        assertNotNull(st.get("x"))
    }

    @Test
    fun `set registra constante en scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("PI", constEntry("PI", TipoDiay(N, N)))
        val entry = st.get("PI")
        assertNotNull(entry)
        assertTrue(entry is ConstanteEntry)
        assertFalse(entry.mutable)
    }

    @Test
    fun `set registra parametro en scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("n", paramEntry("n"))
        val entry = st.get("n")
        assertNotNull(entry)
        assertEquals(Categoria.PARAMETRO, entry.categoria)
    }

    @Test
    fun `set registra funcion en scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("factorial", funcEntry("factorial"))
        val entry = st.get("factorial")
        assertNotNull(entry)
        assertEquals(Categoria.FUNCION, entry.categoria)
    }

    @Test
    fun `set lanza SemanticError si nombre duplicado en mismo scope`() {
        val st = tabla()
        st.newScope()
        st.set("x", varEntry("x"))
        val error = assertFailsWith<SemanticError> {
            st.set("x", varEntry("x"))
        }
        assertEquals("x", error.nombreSimbolo)
    }

    @Test
    fun `set sin scope activo lanza IllegalStateException`() {
        val st = tabla()
        assertFailsWith<IllegalStateException> {
            st.set("x", varEntry("x"))
        }
    }

    @Test
    fun `set permite mismo nombre en scopes distintos (shadowing)`() {
        val st = tabla()
        st.newScope()
        st.set("x", varEntry("x", TipoColones(N, N)))
        st.newScope()
        // No debe lanzar — shadowing es legal
        st.set("x", varEntry("x", TipoDiay(N, N)))
        assertEquals(2, st.depth())
    }

    // -----------------------------------------------------------------------
    // get — búsqueda en la pila
    // -----------------------------------------------------------------------

    @Test
    fun `get devuelve null si nombre no existe`() {
        val st = tabla()
        st.newScope()
        assertNull(st.get("noExiste"))
    }

    @Test
    fun `get encuentra simbolo en scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("y", varEntry("y", TipoLabia(N, N)))
        val entry = st.get("y") as? VariableEntry
        assertNotNull(entry)
        assertTrue(entry.tipoDato is TipoLabia)
    }

    @Test
    fun `get encuentra simbolo en scope padre`() {
        val st = tabla()
        st.newScope()                         // scope 0 (global)
        st.set("global", varEntry("global"))
        st.newScope()                         // scope 1 (función)
        st.newScope()                         // scope 2 (bloque interno)
        // "global" no está en scope 2 ni 1, pero sí en 0
        assertNotNull(st.get("global"))
    }

    @Test
    fun `get devuelve el simbolo del scope mas interno cuando hay shadowing`() {
        val st = tabla()
        st.newScope()
        st.set("x", varEntry("x", TipoColones(N, N)))  // scope 0: x = Colones
        st.newScope()
        st.set("x", varEntry("x", TipoDiay(N, N)))     // scope 1: x = Diay (shadow)

        val entry = st.get("x") as? VariableEntry
        assertNotNull(entry)
        assertTrue(entry.tipoDato is TipoDiay, "Debe retornar el x del scope más interno")
    }

    @Test
    fun `get luego de pop ya no ve simbolos del scope eliminado`() {
        val st = tabla()
        st.newScope()
        st.newScope()
        st.set("local", varEntry("local"))
        st.pop()
        assertNull(st.get("local"), "El símbolo local no debe ser visible tras pop()")
    }

    @Test
    fun `get sin scopes activos devuelve null`() {
        val st = tabla()
        assertNull(st.get("x"))
    }

    // -----------------------------------------------------------------------
    // exists
    // -----------------------------------------------------------------------

    @Test
    fun `exists devuelve true si simbolo existe en algun scope`() {
        val st = tabla()
        st.newScope()
        st.set("existe", varEntry("existe"))
        assertTrue(st.exists("existe"))
    }

    @Test
    fun `exists devuelve false si simbolo no existe`() {
        val st = tabla()
        st.newScope()
        assertFalse(st.exists("noExiste"))
    }

    @Test
    fun `exists devuelve false tras pop del scope que lo contenia`() {
        val st = tabla()
        st.newScope()
        st.newScope()
        st.set("temp", varEntry("temp"))
        st.pop()
        assertFalse(st.exists("temp"))
    }

    // -----------------------------------------------------------------------
    // Categorías y campos específicos
    // -----------------------------------------------------------------------

    @Test
    fun `VariableEntry tiene mutable true por defecto`() {
        val st = tabla()
        st.newScope()
        st.set("v", VariableEntry("v", TipoColones(N, N)))
        val entry = st.get("v") as VariableEntry
        assertTrue(entry.mutable)
        assertEquals(Categoria.VARIABLE, entry.categoria)
    }

    @Test
    fun `ConstanteEntry tiene mutable false por defecto`() {
        val st = tabla()
        st.newScope()
        st.set("C", ConstanteEntry("C", TipoNiPapa(N, N)))
        val entry = st.get("C") as ConstanteEntry
        assertFalse(entry.mutable)
        assertEquals(Categoria.CONSTANTE, entry.categoria)
    }

    @Test
    fun `FuncionEntry almacena firma completa`() {
        val params = listOf(
            Parametro(N, N, TipoColones(N, N), Identificador(N, N, "n"))
        )
        val entry = FuncionEntry(
            nombre = "factorial",
            retorno = TipoColones(N, N),
            params = params,
            cuerpo = Bloque(N, N, emptyList()),
            esEntrada = false
        )
        val st = tabla()
        st.newScope()
        st.set("factorial", entry)

        val retrieved = st.get("factorial") as FuncionEntry
        assertEquals("factorial", retrieved.nombre)
        assertEquals(1, retrieved.params.size)
        assertTrue(retrieved.retorno is TipoColones)
        assertFalse(retrieved.esEntrada)
        assertEquals(Categoria.FUNCION, retrieved.categoria)
    }

    @Test
    fun `FuncionEntry entrada (chante) se registra correctamente`() {
        val entry = FuncionEntry(
            nombre = "chante",
            retorno = TipoNiPapa(N, N),
            params = emptyList(),
            cuerpo = Bloque(N, N, emptyList()),
            esEntrada = true
        )
        val st = tabla()
        st.newScope()
        st.set("chante", entry)

        val retrieved = st.get("chante") as FuncionEntry
        assertTrue(retrieved.esEntrada)
        assertTrue(retrieved.params.isEmpty())
    }

    // -----------------------------------------------------------------------
    // Escenario integrado — simula el análisis de una función con parámetros
    // -----------------------------------------------------------------------

    @Test
    fun `escenario completo - funcion con parametros y variable local`() {
        val st = tabla()

        // Scope global: registrar la función
        st.newScope()
        st.set("suma", funcEntry("suma"))
        assertEquals(1, st.depth())

        // Scope de la función: registrar parámetros
        st.newScope()
        st.set("a", paramEntry("a", TipoColones(N, N)))
        st.set("b", paramEntry("b", TipoColones(N, N)))

        // Scope del bloque interno
        st.newScope()
        st.set("resultado", varEntry("resultado", TipoColones(N, N)))

        // Dentro del bloque se puede ver todo
        assertTrue(st.exists("suma"))
        assertTrue(st.exists("a"))
        assertTrue(st.exists("b"))
        assertTrue(st.exists("resultado"))
        assertEquals(3, st.depth())

        // Salir del bloque
        st.pop()
        assertFalse(st.exists("resultado"), "resultado no visible fuera del bloque")
        assertTrue(st.exists("a"), "parámetros aún visibles en scope de función")

        // Salir de la función
        st.pop()
        assertFalse(st.exists("a"), "parámetros no visibles fuera de la función")
        assertTrue(st.exists("suma"), "función aún visible en scope global")

        // Salir del scope global
        st.pop()
        assertEquals(0, st.depth())
    }

    @Test
    fun `escenario - reasignacion de constante detectada via mutable`() {
        val st = tabla()
        st.newScope()
        st.set("MAX", ConstanteEntry("MAX", TipoColones(N, N), mutable = false))

        val entry = st.get("MAX") as ConstanteEntry
        // El analizador semántico usará entry.mutable para detectar la reasignación ilegal
        assertFalse(entry.mutable, "MAX es constante, no debe ser mutable")
    }

    // -----------------------------------------------------------------------
    // currentScopeEntries y toString
    // -----------------------------------------------------------------------

    @Test
    fun `currentScopeEntries devuelve solo el scope actual`() {
        val st = tabla()
        st.newScope()
        st.set("global", varEntry("global"))
        st.newScope()
        st.set("local", varEntry("local"))

        val entries = st.currentScopeEntries()
        assertTrue(entries.containsKey("local"))
        assertFalse(entries.containsKey("global"), "global no debe aparecer en el scope actual")
    }

    @Test
    fun `toString produce salida no vacia con scopes activos`() {
        val st = tabla()
        st.newScope()
        st.set("x", varEntry("x"))
        val output = st.toString()
        assertTrue(output.contains("x"), "toString debe mencionar el símbolo registrado")
        assertTrue(output.contains("Scope"), "toString debe incluir la palabra Scope")
    }

    @Test
    fun `SemanticError contiene nombre del simbolo y mensaje`() {
        val st = tabla()
        st.newScope()
        st.set("dup", varEntry("dup"))

        val error = assertFailsWith<SemanticError> {
            st.set("dup", varEntry("dup"))
        }
        assertEquals("dup", error.nombreSimbolo)
        assertTrue(error.message!!.contains("dup"))
    }
}