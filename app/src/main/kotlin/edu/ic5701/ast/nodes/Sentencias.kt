package edu.ic5701.ast.nodes

/**
 * Bloque de sentencias delimitado por llaves en CR++.
 * Se usa como cuerpo de funciones, condicionales y ciclos.
 */
data class Bloque(
    override val inicio: Int,
    override val fin: Int,
    val sentencias: List<ASTNode>
) : ASTNode {}

// ---------------------------------------------------------------------------
// Jerarquía de sentencias
// ---------------------------------------------------------------------------

sealed interface Sentencia : ASTNode

/** retornar <expresion> */
data class Retorno(
    override val inicio: Int,
    override val fin: Int,
    val valor: Expresion
) : Sentencia {}

/**
 * si (<condicion>) { <then_body> } [sino { <else_body> }]
 *
 * [elseBody] es null cuando no existe rama sino.
 */
data class Condicional(
    override val inicio: Int,
    override val fin: Int,
    val condicion: Expresion,
    val thenBody: Bloque,
    val elseBody: Bloque?
) : Sentencia {}

/** mientras (<condicion>) { <cuerpo> } */
data class Ciclo(
    override val inicio: Int,
    override val fin: Int,
    val condicion: Expresion,
    val cuerpo: Bloque
) : Sentencia {}

/** salir  — equivalente a break */
data class Ruptura(
    override val inicio: Int,
    override val fin: Int
) : Sentencia {}

/** continuar  — equivalente a continue */
data class Continuar(
    override val inicio: Int,
    override val fin: Int
) : Sentencia {}

/** imprimir(<args>) */
data class Imprimir(
    override val inicio: Int,
    override val fin: Int,
    val args: List<Expresion>
) : Sentencia {}

/** leer(<variable>) */
data class Leer(
    override val inicio: Int,
    override val fin: Int
) : Sentencia {}

/** <nombre> = <valor> */
data class AsignacionSimple(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val valor: Expresion
) : Sentencia {}

/** <nombre>[<indice>] = <valor> */
data class AsignacionArray(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val indice: Expresion,
    val valor: Expresion
) : Sentencia {}

/** Sentencia que consiste únicamente en una expresión (p. ej. llamada a función). */
data class SentenciaExpr(
    override val inicio: Int,
    override val fin: Int,
    val expr: Expresion
) : Sentencia {}
