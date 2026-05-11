package edu.ic5701.ast.nodes

/**
 * Nodo raíz del programa CR++.
 *
 * [cuerpo] contiene todas las declaraciones de nivel superior
 * (variables, constantes y funciones).
 * [entryPoint] siempre es "chante"
 */
data class Programa(
    override val inicio: Int,
    override val fin: Int,
    val cuerpo: List<Declaracion>,
    val sourceType: String = "módulo",
    val entryPoint: String = "chante"
) : ASTNode {}

// ---------------------------------------------------------------------------
// Jerarquía de declaraciones de nivel superior
// ---------------------------------------------------------------------------

sealed interface Declaracion : ASTNode

/**
 * Declaración de variable mutable.
 * Ejemplo CR++:  jalado x: Colones = 5
 */
data class VariableDecl(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val tipoDato: TipoDato,
    val valor: Expresion
) : Declaracion {}

/**
 * Declaración de constante inmutable.
 * Ejemplo CR++:  fijo PI: Diay = 3.14
 */
data class ConstanteDecl(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val tipoDato: TipoDato,
    val valor: Expresion
) : Declaracion {}

/**
 * Declaración de función o función de entrada (main).
 *
 * [esEntrada] indica si es la función principal del programa.
 * [retorno]   es null cuando la función no retorna valor (void).
 * [params]    lista de parámetros formales.
 * [cuerpo]    bloque de sentencias del cuerpo de la función.
 */
data class FuncionDecl(
    override val inicio: Int,
    override val fin: Int,
    val nombre: Identificador,
    val esEntrada: Boolean,
    val retorno: TipoDato,
    val params: List<Parametro>,
    val cuerpo: Bloque
) : Declaracion {}

/**
 * Parámetro formal de una función.
 */
data class Parametro(
    override val inicio: Int,
    override val fin: Int,
    val tipoDato: TipoDato,
    val nombre: Identificador
) : ASTNode {}
