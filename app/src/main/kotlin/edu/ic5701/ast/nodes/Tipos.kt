package edu.ic5701.ast.nodes

/**
 * Jerarquía de nodos que representan tipos de dato en CR++.
 *
 * Nomenclatura costarricense:
 *   - Colones  → entero  (int)
 *   - Diay     → flotante (float)
 *   - Labia    → cadena  (string)
 *   - NiPapa   → booleano (bool)
 *   - Array    → arreglo de cualquier tipo
 */
sealed interface TipoDato : ASTNode

data class TipoColones(
    override val inicio: Int,
    override val fin: Int
) : TipoDato {}

data class TipoDiay(
    override val inicio: Int,
    override val fin: Int
) : TipoDato {}

data class TipoLabia(
    override val inicio: Int,
    override val fin: Int
) : TipoDato {}

data class TipoNiPapa(
    override val inicio: Int,
    override val fin: Int
) : TipoDato {}

data class TipoArray(
    override val inicio: Int,
    override val fin: Int,
    val tipoElemento: TipoDato
) : TipoDato {}
