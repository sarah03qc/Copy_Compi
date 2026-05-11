package edu.ic5701
import edu.ic5701.ast.ASTPrinter
import edu.ic5701.ast.ASTVisitor
import edu.ic5701.ast.accept
import edu.ic5701.ast.nodes.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests de las estructuras de datos del AST de CR++.
 *
 * Verifica que:
 *  - Cada nodo almacena correctamente sus campos.
 *  - Los campos [tipo] coinciden con la especificación del taller.
 *  - Las jerarquías sealed (TipoDato, Declaracion, Sentencia, Expresion)
 *    aceptan todos sus subtipos.
 *  - El Visitor despacha al método correcto para cada nodo.
 *  - El ASTPrinter produce salida coherente (smoke test).
 */
class ASTImplTest {

    // -----------------------------------------------------------------------
    // Helpers — nodos reutilizables
    // -----------------------------------------------------------------------

    private val N = 0          // alias para inicio/fin nulo (= N en el taller)

    private val idX   = Identificador(N, N, "x")
    private val idY   = Identificador(N, N, "y")
    private val lit5  = LiteralEntero(N, N, 5)
    private val lit10 = LiteralEntero(N, N, 10)
    private val litHola = LiteralCadena(N, N, "hola tico")
    private val litTrue  = LiteralBooleano(N, N, true)
    private val litFalse = LiteralBooleano(N, N, false)

    private val bloqueVacio = Bloque(N, N, emptyList())

    // -----------------------------------------------------------------------
    // IDENTIFICADOR
    // -----------------------------------------------------------------------

    @Test
    fun `Identificador almacena nombre y posiciones`() {
        val nodo = Identificador(3, 5, "miVar")
        assertEquals("miVar", nodo.nombre)
        assertEquals(3, nodo.inicio)
        assertEquals(5, nodo.fin)
    }

    // -----------------------------------------------------------------------
    // DECLARACIONES
    // -----------------------------------------------------------------------

    @Test
    fun `VariableDecl almacena todos sus campos`() {
        val nodo = VariableDecl(
            inicio = 0,
            fin = 10,
            nombre = idX,
            tipoDato = TipoColones(N, N),
            valor = lit5
        )
        assertEquals("x", nodo.nombre.nombre)
        assertTrue(nodo.tipoDato is TipoColones)
        assertEquals(5, (nodo.valor as LiteralEntero).valor)
    }

    @Test
    fun `ConstanteDecl almacena todos sus campos`() {
        val nodo = ConstanteDecl(
            inicio = 0,
            fin = 10,
            nombre = idX,
            tipoDato = TipoDiay(N, N),
            valor = LiteralEntero(N, N, 42)
        )
        assertEquals("x", nodo.nombre.nombre)
        assertTrue(nodo.tipoDato is TipoDiay)
    }

    @Test
    fun `FuncionDecl almacena todos sus campos`() {
        val param = Parametro(N, N, TipoColones(N, N), idX)
        val nodo = FuncionDecl(
            inicio = 0,
            fin = 50,
            nombre = Identificador(N, N, "suma"),
            esEntrada = false,
            retorno = TipoColones(N, N),
            params = listOf(param),
            cuerpo = bloqueVacio
        )
        assertEquals("suma", nodo.nombre.nombre)
        assertFalse(nodo.esEntrada)
        assertTrue(nodo.retorno is TipoColones)
        assertEquals(1, nodo.params.size)
    }

    @Test
    fun `FuncionDecl entrada sin retorno es valida`() {
        val nodo = FuncionDecl(
            inicio    = N,
            fin       = N,
            nombre    = Identificador(N, N, "chante"),
            esEntrada = true,
            retorno   = TipoNiPapa(N, N),
            params    = emptyList(),
            cuerpo    = bloqueVacio
        )
        assertTrue(nodo.esEntrada)
        assertEquals(nodo.retorno, TipoNiPapa(N, N))
        assertEquals("chante", nodo.nombre.nombre)
    }

    @Test
    fun `Parametro almacena tipoDato y nombre`() {
        val nodo = Parametro(N, N, TipoLabia(N, N), idY)
        assertTrue(nodo.tipoDato is TipoLabia)
        assertEquals("y", nodo.nombre.nombre)
    }

    @Test
    fun `VariableDecl y ConstanteDecl son Declaracion`() {
        val decls: List<Declaracion> = listOf(
            VariableDecl(N, N, idX, TipoColones(N, N), LiteralNulo(N, N)),
            ConstanteDecl(N, N, idX, TipoColones(N, N), LiteralNulo(N,N)),
            FuncionDecl(N, N, Identificador(N, N, "f"), false, TipoNiPapa(N,N), emptyList(), bloqueVacio)
        )
        assertTrue(decls.all { it is Declaracion })
    }

    // -----------------------------------------------------------------------
    // PROGRAMA
    // -----------------------------------------------------------------------

    @Test
    fun `Programa almacena cuerpo y metadatos`() {
        val decl = VariableDecl(N, N, idX, TipoColones(N, N), lit5)
        val nodo = Programa(inicio = 0, fin = 100, cuerpo = listOf(decl))
        assertEquals("módulo", nodo.sourceType)
        assertEquals("chante", nodo.entryPoint)
        assertEquals(1, nodo.cuerpo.size)
    }

    @Test
    fun `Programa vacio es valido`() {
        val nodo = Programa(N, N, emptyList())
        assertTrue(nodo.cuerpo.isEmpty())
    }

    // -----------------------------------------------------------------------
    // SENTENCIAS
    // -----------------------------------------------------------------------

    @Test
    fun `Bloque almacena sus sentencias`() {
        val sent = Retorno(N, N, lit5)
        val nodo = Bloque(0, 10, listOf(sent))
        assertEquals(1, nodo.sentencias.size)
    }

    @Test
    fun `Retorno almacena su valor`() {
        val nodo = Retorno(N, N, lit10)
        assertEquals(10, (nodo.valor as LiteralEntero).valor)
    }

    @Test
    fun `Condicional con else almacena ambos bloques`() {
        val elseBranch = Bloque(N, N, listOf(Retorno(N, N, litFalse)))
        val nodo = Condicional(
            inicio    = N,
            fin       = N,
            condicion = litTrue,
            thenBody  = bloqueVacio,
            elseBody  = elseBranch
        )
        assertTrue(nodo.condicion is LiteralBooleano)
        assertTrue(nodo.elseBody != null)
    }

    @Test
    fun `Condicional sin else tiene elseBody nulo`() {
        val nodo = Condicional(N, N, litTrue, bloqueVacio, elseBody = null)
        assertNull(nodo.elseBody)
    }

    @Test
    fun `Ciclo almacena condicion y cuerpo`() {
        val nodo = Ciclo(N, N, condicion = litTrue, cuerpo = bloqueVacio)
        assertTrue(nodo.condicion is LiteralBooleano)
    }

    @Test
    fun `Imprimir almacena lista de args`() {
        val nodo = Imprimir(N, N, listOf(litHola, lit5))
        assertEquals(2, nodo.args.size)
    }

    @Test
    fun `AsignacionSimple almacena nombre y valor`() {
        val nodo = AsignacionSimple(N, N, idX, lit5)
        assertEquals("x", nodo.nombre.nombre)
        assertEquals(5, (nodo.valor as LiteralEntero).valor)
    }

    @Test
    fun `AsignacionArray almacena nombre, indice y valor`() {
        val nodo = AsignacionArray(N, N, idX, indice = lit5, valor = lit10)
        assertEquals(5,  (nodo.indice as LiteralEntero).valor)
        assertEquals(10, (nodo.valor  as LiteralEntero).valor)
    }

    @Test
    fun `SentenciaExpr almacena su expresion`() {
        val llamada = LlamadaFuncion(N, N, Identificador(N, N, "foo"), emptyList())
        val nodo = SentenciaExpr(N, N, llamada)
        assertTrue(nodo.expr is LlamadaFuncion)
    }

    @Test
    fun `todas las sentencias son instancia de Sentencia`() {
        val sentencias: List<Sentencia> = listOf(
            Retorno(N, N, lit5),
            Condicional(N, N, litTrue, bloqueVacio, null),
            Ciclo(N, N, litTrue, bloqueVacio),
            Ruptura(N, N),
            Continuar(N, N),
            Imprimir(N, N, emptyList()),
            Leer(N, N),
            AsignacionSimple(N, N, idX, lit5),
            AsignacionArray(N, N, idX, lit5, lit10),
            SentenciaExpr(N, N, lit5)
        )
        assertTrue(sentencias.all { it is Sentencia })
    }

    // -----------------------------------------------------------------------
    // EXPRESIONES — operaciones binarias
    // -----------------------------------------------------------------------

    @Test
    fun `OperacionSuma almacena operandos correctamente`() {
        val nodo = OperacionSuma(N, N, lit5, lit10)
        assertEquals(5,  (nodo.izq as LiteralEntero).valor)
        assertEquals(10, (nodo.der as LiteralEntero).valor)
    }

    @Test
    fun `expresion binaria anidada es valida`() {
        // (5 + 10) * (x - y)
        val suma  = OperacionSuma(N, N, lit5, lit10)
        val resta = OperacionResta(N, N, idX, idY)
        val mul   = OperacionMul(N, N, suma, resta)
        assertTrue(mul.izq is OperacionSuma)
        assertTrue(mul.der is OperacionResta)
    }

    // -----------------------------------------------------------------------
    // EXPRESIONES — operaciones unarias
    // -----------------------------------------------------------------------

    @Test
    fun `OperacionAum almacena objetivo`() {
        val nodo = OperacionAum(N, N, idX)
        assertTrue(nodo.objetivo is Identificador)
    }

    @Test
    fun `OperacionDec almacena objetivo`() {
        val nodo = OperacionDec(N, N, idX)
        assertTrue(nodo.objetivo is Identificador)
    }

    // -----------------------------------------------------------------------
    // FACTORES
    // -----------------------------------------------------------------------

    @Test
    fun `LlamadaFuncion almacena nombre y args`() {
        val nodo = LlamadaFuncion(N, N, Identificador(N, N, "factorial"), listOf(lit5))
        assertEquals("factorial", nodo.nombre.nombre)
        assertEquals(1, nodo.args.size)
    }

    @Test
    fun `LlamadaFuncion sin argumentos es valida`() {
        val nodo = LlamadaFuncion(N, N, Identificador(N, N, "limpiar"), emptyList())
        assertTrue(nodo.args.isEmpty())
    }

    @Test
    fun `AccesoArray almacena nombre e indice`() {
        val nodo = AccesoArray(N, N, idX, lit5)
        assertEquals("x", nodo.nombre.nombre)
        assertEquals(5, (nodo.indice as LiteralEntero).valor)
    }

    @Test
    fun `InicializarArray almacena sus elementos`() {
        val nodo = InicializarArray(N, N, listOf(lit5, lit10))
        assertEquals(2, nodo.elementos.size)
    }

    @Test
    fun `InicializarArray vacio es valido`() {
        val nodo = InicializarArray(N, N, emptyList())
        assertTrue(nodo.elementos.isEmpty())
    }

    // -----------------------------------------------------------------------
    // LITERALES
    // -----------------------------------------------------------------------

    @Test
    fun `LiteralEntero almacena valor entero`() {
        val nodo = LiteralEntero(2, 3, 99)
        assertEquals(99, nodo.valor)
    }

    @Test
    fun `LiteralCadena almacena valor string`() {
        val nodo = LiteralCadena(N, N, "pura vida")
        assertEquals("pura vida", nodo.valor)
    }

    @Test
    fun `LiteralBooleano almacena true y false`() {
        assertEquals(true,  LiteralBooleano(N, N, true).valor)
        assertEquals(false, LiteralBooleano(N, N, false).valor)
    }

    @Test
    fun `todos los literales son Expresion`() {
        val exprs: List<Expresion> = listOf(
            LiteralEntero(N, N, 1),
            LiteralCadena(N, N, "a"),
            LiteralBooleano(N, N, true),
            LiteralNulo(N, N)
        )
        assertTrue(exprs.all { it is Expresion })
    }

    // -----------------------------------------------------------------------
    // VISITOR — despacho correcto
    // -----------------------------------------------------------------------

    /**
     * Visitor mínimo que registra qué método fue invocado.
     * Solo implementa los métodos que usamos en cada test de despacho.
     */
    private inner class SpyVisitor : ASTVisitor<String> {
        override fun visitPrograma(node: Programa)               = "Programa"
        override fun visitTipoColones(node: TipoColones)         = "TipoColones"
        override fun visitTipoDiay(node: TipoDiay)               = "TipoDiay"
        override fun visitTipoLabia(node: TipoLabia)             = "TipoLabia"
        override fun visitTipoNiPapa(node: TipoNiPapa)           = "TipoNiPapa"
        override fun visitTipoArray(node: TipoArray)             = "TipoArray"
        override fun visitVariableDecl(node: VariableDecl)       = "VariableDecl"
        override fun visitConstanteDecl(node: ConstanteDecl)     = "ConstanteDecl"
        override fun visitFuncionDecl(node: FuncionDecl)         = "FuncionDecl"
        override fun visitParametro(node: Parametro)             = "Parametro"
        override fun visitBloque(node: Bloque)                   = "Bloque"
        override fun visitRetorno(node: Retorno)                 = "Retorno"
        override fun visitCondicional(node: Condicional)         = "Condicional"
        override fun visitCiclo(node: Ciclo)                     = "Ciclo"
        override fun visitRuptura(node: Ruptura)                 = "Ruptura"
        override fun visitContinuar(node: Continuar)             = "Continuar"
        override fun visitImprimir(node: Imprimir)               = "Imprimir"
        override fun visitLeer(node: Leer)                       = "Leer"
        override fun visitAsignacionSimple(node: AsignacionSimple) = "AsignacionSimple"
        override fun visitAsignacionArray(node: AsignacionArray) = "AsignacionArray"
        override fun visitSentenciaExpr(node: SentenciaExpr)     = "SentenciaExpr"
        override fun visitOperacionO(node: OperacionO)           = "OperacionO"
        override fun visitOperacionY(node: OperacionY)           = "OperacionY"
        override fun visitOperacionDif(node: OperacionDif)       = "OperacionDif"
        override fun visitOperacionIgu(node: OperacionIgu)       = "OperacionIgu"
        override fun visitOperacionMen(node: OperacionMen)       = "OperacionMen"
        override fun visitOperacionMenIgu(node: OperacionMenIgu) = "OperacionMenIgu"
        override fun visitOperacionMay(node: OperacionMay)       = "OperacionMay"
        override fun visitOperacionMayIgu(node: OperacionMayIgu) = "OperacionMayIgu"
        override fun visitOperacionSuma(node: OperacionSuma)     = "OperacionSuma"
        override fun visitOperacionResta(node: OperacionResta)   = "OperacionResta"
        override fun visitOperacionMul(node: OperacionMul)       = "OperacionMul"
        override fun visitOperacionDiv(node: OperacionDiv)       = "OperacionDiv"
        override fun visitOperacionAum(node: OperacionAum)       = "OperacionAum"
        override fun visitOperacionDec(node: OperacionDec)       = "OperacionDec"
        override fun visitLlamadaFuncion(node: LlamadaFuncion)   = "LlamadaFuncion"
        override fun visitAccesoArray(node: AccesoArray)         = "AccesoArray"
        override fun visitInicializarArray(node: InicializarArray) = "InicializarArray"
        override fun visitIdentificador(node: Identificador)     = "Identificador"
        override fun visitLiteralEntero(node: LiteralEntero)     = "LiteralEntero"
        override fun visitLiteralCadena(node: LiteralCadena)     = "LiteralCadena"
        override fun visitLiteralBooleano(node: LiteralBooleano) = "LiteralBooleano"
        override fun visitLiteralNulo(node: LiteralNulo)         = "LiteralNulo"
    }

    @Test
    fun `visitor despachado correctamente para cada nodo`() {
        val spy = SpyVisitor()

        val nodos: List<Pair<ASTNode, String>> = listOf(
            Programa(N, N, emptyList())                                          to "Programa",
            TipoColones(N, N)                                                    to "TipoColones",
            TipoDiay(N, N)                                                       to "TipoDiay",
            TipoLabia(N, N)                                                      to "TipoLabia",
            TipoNiPapa(N, N)                                                     to "TipoNiPapa",
            TipoArray(N, N, TipoColones(N, N))                                   to "TipoArray",
            VariableDecl(N, N, idX, TipoColones(N, N), LiteralNulo(N, N))                    to "VariableDecl",
            ConstanteDecl(N, N, idX, TipoColones(N, N), LiteralNulo(N, N))                   to "ConstanteDecl",
            FuncionDecl(N, N, idX, false, TipoNiPapa(N,N), emptyList(), bloqueVacio)        to "FuncionDecl",
            Parametro(N, N, TipoColones(N, N), idX)                             to "Parametro",
            Bloque(N, N, emptyList())                                            to "Bloque",
            Retorno(N, N, lit5)                                                  to "Retorno",
            Condicional(N, N, litTrue, bloqueVacio, null)                        to "Condicional",
            Ciclo(N, N, litTrue, bloqueVacio)                                    to "Ciclo",
            Ruptura(N, N)                                                        to "Ruptura",
            Continuar(N, N)                                                      to "Continuar",
            Imprimir(N, N, emptyList())                                          to "Imprimir",
            Leer(N, N)                                                           to "Leer",
            AsignacionSimple(N, N, idX, lit5)                                   to "AsignacionSimple",
            AsignacionArray(N, N, idX, lit5, lit10)                             to "AsignacionArray",
            SentenciaExpr(N, N, lit5)                                            to "SentenciaExpr",
            OperacionO(N, N, lit5, lit10)                                        to "OperacionO",
            OperacionY(N, N, lit5, lit10)                                        to "OperacionY",
            OperacionDif(N, N, lit5, lit10)                                      to "OperacionDif",
            OperacionIgu(N, N, lit5, lit10)                                      to "OperacionIgu",
            OperacionMen(N, N, lit5, lit10)                                      to "OperacionMen",
            OperacionMenIgu(N, N, lit5, lit10)                                   to "OperacionMenIgu",
            OperacionMay(N, N, lit5, lit10)                                      to "OperacionMay",
            OperacionMayIgu(N, N, lit5, lit10)                                   to "OperacionMayIgu",
            OperacionSuma(N, N, lit5, lit10)                                     to "OperacionSuma",
            OperacionResta(N, N, lit5, lit10)                                    to "OperacionResta",
            OperacionMul(N, N, lit5, lit10)                                      to "OperacionMul",
            OperacionDiv(N, N, lit5, lit10)                                      to "OperacionDiv",
            OperacionAum(N, N, idX)                                              to "OperacionAum",
            OperacionDec(N, N, idX)                                              to "OperacionDec",
            LlamadaFuncion(N, N, idX, emptyList())                              to "LlamadaFuncion",
            AccesoArray(N, N, idX, lit5)                                         to "AccesoArray",
            InicializarArray(N, N, emptyList())                                  to "InicializarArray",
            Identificador(N, N, "z")                                             to "Identificador",
            LiteralEntero(N, N, 1)                                               to "LiteralEntero",
            LiteralCadena(N, N, "s")                                             to "LiteralCadena",
            LiteralBooleano(N, N, true)                                          to "LiteralBooleano",
            LiteralNulo(N, N)                                                    to "LiteralNulo"
        )

        for ((nodo, esperado) in nodos) {
            val resultado = nodo.accept(spy)
            assertEquals(esperado, resultado, "despacho incorrecto para nodo $esperado")
        }
    }

    // -----------------------------------------------------------------------
    // ASTPRINTER — smoke test
    // -----------------------------------------------------------------------

    @Test
    fun `ASTPrinter no lanza excepcion para un programa completo`() {
        val programa = Programa(
            inicio = 0,
            fin    = 99,
            cuerpo = listOf(
                FuncionDecl(
                    inicio    = 0,
                    fin       = 99,
                    nombre    = Identificador(N, N, "chante"),
                    esEntrada = true,
                    retorno   = TipoNiPapa(N,N),
                    params    = emptyList(),
                    cuerpo    = Bloque(
                        inicio     = N,
                        fin        = N,
                        sentencias = listOf(
                            VariableDecl(N, N, idX, TipoColones(N, N), lit5),
                            Condicional(
                                inicio    = N,
                                fin       = N,
                                condicion = OperacionMay(N, N, idX, lit10),
                                thenBody  = Bloque(N, N, listOf(Imprimir(N, N, listOf(litHola)))),
                                elseBody  = Bloque(N, N, listOf(Retorno(N, N, litFalse)))
                            ),
                            AsignacionSimple(N, N, idX, OperacionSuma(N, N, idX, lit5))
                        )
                    )
                )
            )
        )

        // Redirigir stdout para no ensuciar la salida del test runner
        val originalOut = System.out
        val sink = java.io.PrintStream(java.io.ByteArrayOutputStream())
        System.setOut(sink)
        try {
            programa.accept(ASTPrinter())
        } finally {
            System.setOut(originalOut)
        }
        // Si llegamos aquí sin excepción, el printer funciona
    }
}
