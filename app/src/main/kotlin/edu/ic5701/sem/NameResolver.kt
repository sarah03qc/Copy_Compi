package edu.ic5701.sem

import edu.ic5701.ast.ASTVisitor
import edu.ic5701.ast.nodes.*
import edu.ic5701.symboltable.*

/**
 * Analizador semántico — Resolución de nombres (T9b).
 *
 * Responsabilidad: recorrer el AST y verificar que cada identificador
 * que se usa haya sido declarado previamente en algún ámbito visible,
 * y que cada declaración no duplique un nombre en el mismo ámbito.
 *
 * Lo que NO hace este visitor: chequeo de tipos en operaciones.
 * Eso corresponde a T9c (TypeChecker).
 *
 * Estrategia:
 *   - Implementa [ASTVisitor] con tipo de retorno [Unit].
 *   - Acumula todos los errores en [errores] en lugar de lanzar
 *     en el primer problema, para dar feedback completo.
 *   - Usa [StackSymbolTable] como tabla de símbolos.
 *
 * Uso:
 *   val resolver = NameResolver()
 *   resolver.visitPrograma(ast)
 *   if (resolver.tieneErrores()) { resolver.errores.forEach { println(it) } }
 */
class NameResolver : ASTVisitor<Unit> {

    val errores: MutableList<String> = mutableListOf()

    private val tabla: StackSymbolTable = StackSymbolTable()

    // -----------------------------------------------------------------------
    // Utilidades internas
    // -----------------------------------------------------------------------

    private fun reportar(mensaje: String) {
        errores.add("Error semántico: $mensaje")
    }

    fun tieneErrores(): Boolean = errores.isNotEmpty()

    // -----------------------------------------------------------------------
    // Raíz del programa
    // -----------------------------------------------------------------------

    /**
     * Procesa el programa en dos pasadas:
     *   1. Registra todas las funciones en el scope global (para permitir
     *      llamadas mutuas sin importar el orden de declaración).
     *   2. Analiza el cuerpo de cada función.
     *
     * Al final verifica que exista exactamente una función de entrada
     * con nombre "chante", retorno TipoNiPapa y sin parámetros.
     */
    override fun visitPrograma(node: Programa) {
        tabla.newScope() // scope global

        // Pasada 1: registrar firmas de todas las funciones
        for (decl in node.cuerpo) {
            if (decl is FuncionDecl) {
                registrarFuncion(decl)
            }
        }

        // Verificar punto de entrada
        verificarEntrada(node.cuerpo)

        // Pasada 2: analizar el cuerpo de cada función
        for (decl in node.cuerpo) {
            when (decl) {
                is FuncionDecl -> visitFuncionDecl(decl)
                is VariableDecl -> reportar(
                    "declaración de variable '${decl.nombre.nombre}' en nivel global no permitida"
                )
                is ConstanteDecl -> reportar(
                    "declaración de constante '${decl.nombre.nombre}' en nivel global no permitida"
                )
            }
        }

        // No hacemos pop del scope global; el análisis terminó.
    }

    private fun registrarFuncion(decl: FuncionDecl) {
        try {
            tabla.set(
                decl.nombre.nombre,
                FuncionEntry(
                    nombre = decl.nombre.nombre,
                    retorno = decl.retorno,
                    params = decl.params,
                    cuerpo = decl.cuerpo,
                    esEntrada = decl.esEntrada
                )
            )
        } catch (e: SemanticError) {
            reportar("función '${decl.nombre.nombre}' ya está declarada")
        }
    }

    private fun verificarEntrada(cuerpo: List<Declaracion>) {
        val entradas = cuerpo.filterIsInstance<FuncionDecl>().filter { it.esEntrada }

        when {
            entradas.isEmpty() ->
                reportar("el programa no tiene función de entrada 'chante'")
            entradas.size > 1 ->
                reportar("el programa tiene más de una función de entrada")
            else -> {
                val chante = entradas.first()
                if (chante.nombre.nombre != "chante") {
                    reportar(
                        "la función de entrada debe llamarse 'chante', " +
                        "se encontró '${chante.nombre.nombre}'"
                    )
                }
                if (chante.retorno !is TipoNiPapa) {
                    reportar("'chante' debe retornar ni_papa (void)")
                }
                if (chante.params.isNotEmpty()) {
                    reportar("'chante' no debe tener parámetros")
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Declaraciones
    // -----------------------------------------------------------------------

    /**
     * Abre un scope nuevo para la función, registra los parámetros
     * y analiza el cuerpo.
     * El scope de la función envuelve al scope del bloque principal,
     * por lo que los parámetros son visibles dentro del cuerpo.
     */
    override fun visitFuncionDecl(node: FuncionDecl) {
        tabla.newScope()

        for (param in node.params) {
            visitParametro(param)
        }

        // Analizar las sentencias del cuerpo directamente sin abrir un scope
        // adicional para el bloque — el scope de la función ya cumple esa función.
        for (sentencia in node.cuerpo.sentencias) {
            analizarNodo(sentencia)
        }

        tabla.pop()
    }

    override fun visitParametro(node: Parametro) {
        try {
            tabla.set(
                node.nombre.nombre,
                ParametroEntry(nombre = node.nombre.nombre, tipoDato = node.tipoDato)
            )
        } catch (e: SemanticError) {
            reportar("parámetro '${node.nombre.nombre}' duplicado en la misma función")
        }
    }

    override fun visitVariableDecl(node: VariableDecl) {
        // Primero analizar la expresión de valor (puede referenciar nombres existentes)
        analizarExpresion(node.valor)

        try {
            tabla.set(
                node.nombre.nombre,
                VariableEntry(nombre = node.nombre.nombre, tipoDato = node.tipoDato)
            )
        } catch (e: SemanticError) {
            reportar("variable '${node.nombre.nombre}' ya está declarada en este ámbito")
        }
    }

    override fun visitConstanteDecl(node: ConstanteDecl) {
        analizarExpresion(node.valor)

        try {
            tabla.set(
                node.nombre.nombre,
                ConstanteEntry(nombre = node.nombre.nombre, tipoDato = node.tipoDato)
            )
        } catch (e: SemanticError) {
            reportar("constante '${node.nombre.nombre}' ya está declarada en este ámbito")
        }
    }

    // -----------------------------------------------------------------------
    // Sentencias
    // -----------------------------------------------------------------------

    /**
     * Bloque: abre un nuevo scope, analiza sus sentencias, cierra el scope.
     */
    override fun visitBloque(node: Bloque) {
        tabla.newScope()
        for (sentencia in node.sentencias) {
            analizarNodo(sentencia)
        }
        tabla.pop()
    }

    override fun visitRetorno(node: Retorno) {
        analizarExpresion(node.valor)
    }

    override fun visitCondicional(node: Condicional) {
        analizarExpresion(node.condicion)
        visitBloque(node.thenBody)
        node.elseBody?.let { visitBloque(it) }
    }

    override fun visitCiclo(node: Ciclo) {
        analizarExpresion(node.condicion)
        visitBloque(node.cuerpo)
    }

    override fun visitRuptura(node: Ruptura) { /* sin hijos */ }

    override fun visitContinuar(node: Continuar) { /* sin hijos */ }

    override fun visitImprimir(node: Imprimir) {
        for (arg in node.args) analizarExpresion(arg)
    }

    override fun visitLeer(node: Leer) { /* sin hijos */ }

    override fun visitAsignacionSimple(node: AsignacionSimple) {
        // Verificar que la variable de destino existe
        visitIdentificador(node.nombre)

        // Verificar que no es constante (mutable = false)
        val entry = tabla.get(node.nombre.nombre)
        if (entry != null && !entry.mutableEntry()) {
            reportar("no se puede reasignar la constante '${node.nombre.nombre}'")
        }

        analizarExpresion(node.valor)
    }

    override fun visitAsignacionArray(node: AsignacionArray) {
        visitIdentificador(node.nombre)

        val entry = tabla.get(node.nombre.nombre)
        if (entry != null && !entry.mutableEntry()) {
            reportar("no se puede reasignar la constante '${node.nombre.nombre}'")
        }

        analizarExpresion(node.indice)
        analizarExpresion(node.valor)
    }

    override fun visitSentenciaExpr(node: SentenciaExpr) {
        analizarExpresion(node.expr)
    }

    // -----------------------------------------------------------------------
    // Factores y expresiones atómicas
    // -----------------------------------------------------------------------

    /**
     * Punto central de resolución de nombres:
     * verifica que el identificador esté declarado en algún scope visible.
     */
    override fun visitIdentificador(node: Identificador) {
        if (!tabla.exists(node.nombre)) {
            reportar("identificador '${node.nombre}' no está declarado")
        }
    }

    override fun visitLlamadaFuncion(node: LlamadaFuncion) {
        // Verificar que el nombre de la función existe
        if (!tabla.exists(node.nombre.nombre)) {
            reportar("función '${node.nombre.nombre}' no está declarada")
        } else {
            val entry = tabla.get(node.nombre.nombre)
            if (entry !is FuncionEntry) {
                reportar("'${node.nombre.nombre}' no es una función")
            } else {
                // Verificar cantidad de argumentos
                if (node.args.size != entry.params.size) {
                    reportar(
                        "función '${node.nombre.nombre}' espera ${entry.params.size} " +
                        "argumento(s), se pasaron ${node.args.size}"
                    )
                }
            }
        }
        for (arg in node.args) analizarExpresion(arg)
    }

    override fun visitAccesoArray(node: AccesoArray) {
        visitIdentificador(node.nombre)
        analizarExpresion(node.indice)
    }

    override fun visitInicializarArray(node: InicializarArray) {
        if (node.elementos.isEmpty()) {
            reportar("un arreglo literal debe tener al menos un elemento")
        }
        for (elem in node.elementos) analizarExpresion(elem)
    }

    // -----------------------------------------------------------------------
    // Literales — no hay nombres que resolver
    // -----------------------------------------------------------------------

    override fun visitLiteralEntero(node: LiteralEntero) {}
    override fun visitLiteralCadena(node: LiteralCadena) {}
    override fun visitLiteralBooleano(node: LiteralBooleano) {}
    override fun visitLiteralNulo(node: LiteralNulo) {}

    // -----------------------------------------------------------------------
    // Operaciones binarias — delegar a subárboles
    // -----------------------------------------------------------------------

    override fun visitOperacionO(node: OperacionO)           { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionY(node: OperacionY)           { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionDif(node: OperacionDif)       { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionIgu(node: OperacionIgu)       { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionMen(node: OperacionMen)       { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionMenIgu(node: OperacionMenIgu) { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionMay(node: OperacionMay)       { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionMayIgu(node: OperacionMayIgu) { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionSuma(node: OperacionSuma)     { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionResta(node: OperacionResta)   { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionMul(node: OperacionMul)       { analizarExpresion(node.izq); analizarExpresion(node.der) }
    override fun visitOperacionDiv(node: OperacionDiv)       { analizarExpresion(node.izq); analizarExpresion(node.der) }

    // -----------------------------------------------------------------------
    // Operaciones unarias
    // -----------------------------------------------------------------------

    override fun visitOperacionAum(node: OperacionAum) { analizarExpresion(node.objetivo) }
    override fun visitOperacionDec(node: OperacionDec) { analizarExpresion(node.objetivo) }

    // -----------------------------------------------------------------------
    // Tipos — solo se usan como metadatos, no contienen nombres que resolver
    // -----------------------------------------------------------------------

    override fun visitTipoColones(node: TipoColones) {}
    override fun visitTipoDiay(node: TipoDiay) {}
    override fun visitTipoLabia(node: TipoLabia) {}
    override fun visitTipoNiPapa(node: TipoNiPapa) {}
    override fun visitTipoArray(node: TipoArray) {}

    // -----------------------------------------------------------------------
    // Helpers privados de despacho
    // -----------------------------------------------------------------------

    private fun analizarExpresion(expr: Expresion) {
        expr.accept(this)
    }

    private fun analizarNodo(nodo: ASTNode) {
        nodo.accept(this)
    }
}

// ---------------------------------------------------------------------------
// Extensión auxiliar para leer mutabilidad de cualquier SymbolEntry
// ---------------------------------------------------------------------------

private fun SymbolEntry.mutableEntry(): Boolean = when (this) {
    is VariableEntry   -> mutable
    is ConstanteEntry  -> mutable
    is ParametroEntry  -> mutable
    is FuncionEntry    -> false
}
