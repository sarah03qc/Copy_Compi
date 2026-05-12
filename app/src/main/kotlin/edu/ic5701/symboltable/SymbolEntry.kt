package edu.ic5701.symboltable

import edu.ic5701.ast.nodes.Bloque
import edu.ic5701.ast.nodes.Parametro
import edu.ic5701.ast.nodes.TipoDato


/**
 * Categoría de un símbolo dentro de la tabla.
 * Refleja exactamente las categorías del taller:
 *   variable | constante | parámetro | función
 */
enum class Categoria {
    VARIABLE,
    CONSTANTE,
    PARAMETRO,
    FUNCION
}

/**
 * Entrada base en la tabla de símbolos.
 * Todos los símbolos tienen nombre y categoría.
 */
sealed interface SymbolEntry {
    val nombre: String
    val categoria: Categoria
}

/**
 * Entrada para variables declaradas con `jalado`.
 *
 * [mutable] = true  (las variables siempre son mutables)
 * [tipoDato] puede ser null si aún no fue inferido (durante construcción).
 */
data class VariableEntry(
    override val nombre: String,
    val tipoDato: TipoDato,
    val mutable: Boolean = true
) : SymbolEntry {
    override val categoria: Categoria = Categoria.VARIABLE
}

/**
 * Entrada para constantes declaradas con `fijo`.
 *
 * [mutable] = false (las constantes nunca son mutables)
 */
data class ConstanteEntry(
    override val nombre: String,
    val tipoDato: TipoDato,
    val mutable: Boolean = false
) : SymbolEntry {
    override val categoria: Categoria = Categoria.CONSTANTE
}

/**
 * Entrada para parámetros formales de una función.
 * Desde el punto de vista de la tabla son como variables,
 * pero con categoría PARAMETRO para distinguirlos.
 */
data class ParametroEntry(
    override val nombre: String,
    val tipoDato: TipoDato,
    val mutable: Boolean = true
) : SymbolEntry {
    override val categoria: Categoria = Categoria.PARAMETRO
}

/**
 * Entrada para funciones declaradas.
 *
 * Almacena la firma completa para poder verificar llamadas:
 * cantidad de argumentos, tipos y tipo de retorno.
 *
 * [esEntrada] indica si es la función principal (chante).
 */
data class FuncionEntry(
    override val nombre: String,
    val retorno: TipoDato,
    val params: List<Parametro>,
    val cuerpo: Bloque,
    val esEntrada: Boolean
) : SymbolEntry {
    override val categoria: Categoria = Categoria.FUNCION
}