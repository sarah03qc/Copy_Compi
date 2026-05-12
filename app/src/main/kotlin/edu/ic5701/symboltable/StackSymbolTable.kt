package edu.ic5701.symboltable

/**
 * Implementación de [SymbolTable] basada en una pila de ámbitos.
 *
 * Cada scope es un [LinkedHashMap] que preserva el orden de inserción,
 * útil para depuración y para iterar los símbolos en orden declarativo.
 *
 * La pila crece hacia adelante: el último elemento es el scope más interno.
 *
 *   Scopes = [ globalScope, funciónScope, bloqueScope ]
 *                                                ↑ current
 *
 * Convención del proyecto:
 *   Interfaz → SymbolTable
 *   Implementación → StackSymbolTable
 */
class StackSymbolTable : SymbolTable {

    // Pila de scopes. Cada scope mapea nombre → SymbolEntry.
    private val scopes: ArrayDeque<LinkedHashMap<String, SymbolEntry>> = ArrayDeque()

    // -----------------------------------------------------------------------
    // Gestión de scopes
    // -----------------------------------------------------------------------

    /**
     * Apila un nuevo scope vacío.
     * Llamar al entrar a cualquier bloque { } o al procesar una función.
     */
    override fun newScope() {
        scopes.addLast(LinkedHashMap())
    }

    /**
     * Descarta el scope más interno.
     * Llamar al salir del bloque o función correspondiente.
     *
     * @throws IllegalStateException si la pila está vacía.
     */
    override fun pop() {
        check(scopes.isNotEmpty()) {
            "StackSymbolTable: pop() llamado con la pila vacía"
        }
        scopes.removeLast()
    }

    // -----------------------------------------------------------------------
    // Registro y búsqueda
    // -----------------------------------------------------------------------

    /**
     * Registra [entry] en el scope actual.
     *
     * Lanza [SemanticError] si [nombre] ya está definido en este mismo scope
     * (no en scopes padres — el shadowing entre scopes distintos es legal).
     */
    override fun set(nombre: String, entry: SymbolEntry) {
        val currentScope = currentScope()
        if (currentScope.containsKey(nombre)) {
            throw SemanticError(
                mensaje = "el nombre '$nombre' ya está declarado en este ámbito",
                nombreSimbolo = nombre
            )
        }
        currentScope[nombre] = entry
    }

    /**
     * Busca [nombre] desde el scope más interno hacia el más externo.
     * Devuelve null si no se encuentra en ningún nivel.
     */
    override fun get(nombre: String): SymbolEntry? {
        // Recorrer de más interno (último) a más externo (primero)
        for (i in scopes.indices.reversed()) {
            scopes[i][nombre]?.let { return it }
        }
        return null
    }

    /**
     * Devuelve true si [nombre] existe en algún scope visible.
     */
    override fun exists(nombre: String): Boolean = get(nombre) != null

    // -----------------------------------------------------------------------
    // Helpers internos
    // -----------------------------------------------------------------------

    /**
     * Devuelve el scope actual (el más interno).
     *
     * @throws IllegalStateException si no hay ningún scope activo.
     *         Esto indicaría un bug en el analizador semántico — siempre
     *         debe haber al menos el scope global activo.
     */
    private fun currentScope(): LinkedHashMap<String, SymbolEntry> {
        check(scopes.isNotEmpty()) {
            "StackSymbolTable: no hay ningún scope activo. " +
                    "¿Olvidaste llamar a newScope() antes de set()?"
        }
        return scopes.last()
    }

    // -----------------------------------------------------------------------
    // Utilidades de depuración
    // -----------------------------------------------------------------------

    /**
     * Devuelve el número de scopes activos en la pila.
     * Útil para tests y para mensajes de depuración.
     */
    fun depth(): Int = scopes.size

    /**
     * Devuelve todos los símbolos del scope actual (sin los padres).
     * Útil para verificar el contenido de un scope en tests.
     */
    fun currentScopeEntries(): Map<String, SymbolEntry> =
        if (scopes.isEmpty()) emptyMap() else currentScope().toMap()

    /**
     * Representación textual de toda la pila de scopes.
     * Formato:
     *   Scope 0 (global):
     *     suma → FuncionEntry(...)
     *   Scope 1:
     *     x → VariableEntry(...)
     */
    override fun toString(): String = buildString {
        scopes.forEachIndexed { index, scope ->
            val label = "Scope $index" + if (index == 0) " (global)" else ""
            appendLine("$label:")
            if (scope.isEmpty()) {
                appendLine("  (vacío)")
            } else {
                scope.forEach { (nombre, entry) ->
                    appendLine("  $nombre → $entry")
                }
            }
        }
    }
}