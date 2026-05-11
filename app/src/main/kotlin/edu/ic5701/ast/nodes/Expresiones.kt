package edu.ic5701.ast.nodes

/**
 * Jerarquía de expresiones del AST de CR++.
 *
 * Todas las operaciones binarias tienen [izq] y [der].
 * Las operaciones unarias (incremento/decremento) tienen sólo [objetivo].
 */
sealed interface Expresion : ASTNode

// ---------------------------------------------------------------------------
// Operaciones binarias de comparación
// ---------------------------------------------------------------------------

/** izq == der */
data class OperacionO(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq && der  (Y lógico) */
data class OperacionY(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq != der */
data class OperacionDif(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq == der  (igualdad) */
data class OperacionIgu(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq < der */
data class OperacionMen(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq <= der */
data class OperacionMenIgu(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq > der */
data class OperacionMay(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq >= der */
data class OperacionMayIgu(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

// ---------------------------------------------------------------------------
// Operaciones binarias aritméticas
// ---------------------------------------------------------------------------

/** izq + der */
data class OperacionSuma(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq - der */
data class OperacionResta(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion

/** izq * der */
data class OperacionMul(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

/** izq / der */
data class OperacionDiv(
    override val inicio: Int,
    override val fin: Int,
    val izq: Expresion,
    val der: Expresion
) : Expresion {}

// ---------------------------------------------------------------------------
// Operaciones unarias
// ---------------------------------------------------------------------------

/** ++objetivo */
data class OperacionAum(
    override val inicio: Int,
    override val fin: Int,
    val objetivo: Expresion
) : Expresion {}

/** --objetivo */
data class OperacionDec(
    override val inicio: Int,
    override val fin: Int,
    val objetivo: Expresion
) : Expresion {}
