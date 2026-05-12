package edu.ic5701.symboltable

/**
 * Interfaz de la tabla de símbolos de CR++.
 *
 * Modela una pila de scopes (ámbitos anidados).  Cada llamada a [newScope]
 * apila un nuevo ámbito vacío; cada llamada a [pop] lo descarta.
 *
 * Convención de nomenclatura:
 *   - Scanner  → TableDrivenScanner
 *   - SymbolTable → StackSymbolTable   (implementación de esta interfaz)
 */
interface SymbolTable {

    /**
     * Crea un nuevo ámbito (scope) y lo apila encima del actual.
     * Se llama al entrar a un bloque o al procesar el cuerpo de una función.
     */
    fun newScope()

    /**
     * Descarta el ámbito más reciente.
     * Se llama al salir de un bloque o del cuerpo de una función.
     *
     * @throws IllegalStateException si no hay ningún scope que descartar.
     */
    fun pop()

    /**
     * Registra [entry] en el ámbito actual bajo [nombre].
     *
     * @throws SemanticError si [nombre] ya está definido en el ámbito actual
     *         (no en scopes padres — shadowing está permitido).
     */
    fun set(nombre: String, entry: SymbolEntry)

    /**
     * Busca [nombre] empezando en el ámbito más interno y subiendo
     * hacia los scopes padres hasta encontrarlo o agotar la pila.
     *
     * @return la entrada encontrada, o null si no existe en ningún scope.
     */
    fun get(nombre: String): SymbolEntry?

    /**
     * Indica si [nombre] existe en algún scope visible desde el actual.
     * Equivalente a `get(nombre) != null`.
     */
    fun exists(nombre: String): Boolean
}