package edu.ic5701.ast.nodes

/**
 * Factores: unidades atómicas o compuestas que forman expresiones.
 * Todos implementan [Expresion] pues pueden aparecer en cualquier posición
 * donde se espere un valor.
 */

// ---------------------------------------------------------------------------
// Llamada a función
// ---------------------------------------------------------------------------

/**
 * Invocación de función: nombre(arg1, arg2, ...)
 */
data class LlamadaFuncion(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val args: List<Expresion>
) : Expresion {}

// ---------------------------------------------------------------------------
// Acceso e inicialización de arreglos
// ---------------------------------------------------------------------------

/** nombre[indice] — lectura de una posición del arreglo */
data class AccesoArray(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val indice: Expresion
) : Expresion {}

/** [e1, e2, ..., eN] — literal de arreglo */
data class InicializarArray(
    override val inicio: Int,
    override val fin: Int,
    val elementos: List<Expresion>
) : Expresion {}

// ---------------------------------------------------------------------------
// Identificador
// ---------------------------------------------------------------------------

/**
 * Referencia a un nombre (variable, constante o función).
 * [nombre] es el lexema tal como aparece en el código fuente.
 */
data class Identificador(
    override val inicio: Int,
    override val fin: Int,
    val nombre: String
) : Expresion {}

// ---------------------------------------------------------------------------
// Literales
// ---------------------------------------------------------------------------

/** Literal entero, p. ej.  42 */
data class LiteralEntero(
    override val inicio: Int,
    override val fin: Int,
    val valor: Int
) : Expresion {}

/** Literal de cadena, p. ej.  "hola tico" */
data class LiteralCadena(
    override val inicio: Int,
    override val fin: Int,
    val valor: String
) : Expresion {}

/** Literal booleano: verdá | miente */
data class LiteralBooleano(
    override val inicio: Int,
    override val fin: Int,
    val valor: Boolean
) : Expresion {}

/** Literal nulo: nada */
data class LiteralNulo(
    override val inicio: Int,
    override val fin: Int
) : Expresion {}
