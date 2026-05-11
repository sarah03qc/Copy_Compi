package edu.ic5701.ast

import edu.ic5701.ast.nodes.*

/**
 * Visitor concreto que imprime el AST en formato indentado.
 *
 * Útil para verificar que el parser construye el árbol correctamente.
 *
 * Uso:
 * ```kotlin
 * val printer = ASTPrinter()
 * programa.accept(printer)
 * ```
 *
 * Salida de ejemplo:
 * ```
 * Programa [0..N]
 *   FuncionDecl 'chante' entrada=true [2..45]
 *     params: (ninguno)
 *     retorno: (ninguno)
 *     cuerpo:
 *       Bloque [4..43]
 *         Imprimir [5..20]
 *           LiteralCadena "hola tico" [14..19]
 * ```
 */
class ASTPrinter : ASTVisitor<Unit> {

    private var indent = 0

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun line(text: String) = println("  ".repeat(indent) + text)

    private fun pos(node: ASTNode) = "[${node.inicio ?: "?"}..${node.fin ?: "?"}]"

    private inline fun indented(block: () -> Unit) {
        indent++
        block()
        indent--
    }

    private fun visitTipo(tipo: TipoDato?) {
        if (tipo == null) line("retorno: (ninguno)")
        else tipo.accept(this)
    }

    // -----------------------------------------------------------------------
    // Raíz
    // -----------------------------------------------------------------------

    override fun visitPrograma(node: Programa) {
        line("Programa ${pos(node)}  sourceType=${node.sourceType}  entryPoint=${node.entryPoint}")
        indented {
            if (node.cuerpo.isEmpty()) line("(sin declaraciones)")
            else node.cuerpo.forEach { it.accept(this) }
        }
    }

    // -----------------------------------------------------------------------
    // Tipos
    // -----------------------------------------------------------------------

    override fun visitTipoColones(node: TipoColones)   = line("TipoColones ${pos(node)}")
    override fun visitTipoDiay(node: TipoDiay)         = line("TipoDiay ${pos(node)}")
    override fun visitTipoLabia(node: TipoLabia)       = line("TipoLabia ${pos(node)}")
    override fun visitTipoNiPapa(node: TipoNiPapa)     = line("TipoNiPapa ${pos(node)}")
    override fun visitTipoArray(node: TipoArray) {
        line("TipoArray ${pos(node)}")
        indented { node.tipoElemento.accept(this) }
    }

    // -----------------------------------------------------------------------
    // Declaraciones
    // -----------------------------------------------------------------------

    override fun visitVariableDecl(node: VariableDecl) {
        line("VariableDecl '${node.nombre.nombre}' ${pos(node)}")
        indented {
            node.tipoDato.accept(this)
            if (node.valor != null) node.valor.accept(this)
            else line("valor: (ninguno)")
        }
    }

    override fun visitConstanteDecl(node: ConstanteDecl) {
        line("ConstanteDecl '${node.nombre.nombre}' ${pos(node)}")
        indented {
            node.tipoDato.accept(this)
            if (node.valor != null) node.valor.accept(this)
            else line("valor: (ninguno)")
        }
    }

    override fun visitFuncionDecl(node: FuncionDecl) {
        line("FuncionDecl '${node.nombre.nombre}' entrada=${node.esEntrada} ${pos(node)}")
        indented {
            // retorno
            if (node.retorno != null) { line("retorno:"); indented { node.retorno.accept(this) } }
            else line("retorno: (ninguno)")

            // params
            if (node.params.isEmpty()) line("params: (ninguno)")
            else { line("params:"); indented { node.params.forEach { it.accept(this) } } }

            // cuerpo
            line("cuerpo:"); indented { node.cuerpo.accept(this) }
        }
    }

    override fun visitParametro(node: Parametro) {
        line("Parametro '${node.nombre.nombre}' ${pos(node)}")
        indented { node.tipoDato.accept(this) }
    }

    // -----------------------------------------------------------------------
    // Sentencias
    // -----------------------------------------------------------------------

    override fun visitBloque(node: Bloque) {
        line("Bloque ${pos(node)}")
        indented {
            if (node.sentencias.isEmpty()) line("(vacío)")
            else node.sentencias.forEach { it.accept(this) }
        }
    }

    override fun visitRetorno(node: Retorno) {
        line("Retorno ${pos(node)}")
        indented { node.valor.accept(this) }
    }

    override fun visitCondicional(node: Condicional) {
        line("Condicional ${pos(node)}")
        indented {
            line("condicion:"); indented { node.condicion.accept(this) }
            line("then:"); indented { node.thenBody.accept(this) }
            if (node.elseBody != null) { line("sino:"); indented { node.elseBody.accept(this) } }
        }
    }

    override fun visitCiclo(node: Ciclo) {
        line("Ciclo ${pos(node)}")
        indented {
            line("condicion:"); indented { node.condicion.accept(this) }
            line("cuerpo:");    indented { node.cuerpo.accept(this) }
        }
    }

    override fun visitRuptura(node: Ruptura)   = line("Ruptura ${pos(node)}")
    override fun visitContinuar(node: Continuar) = line("Continuar ${pos(node)}")

    override fun visitImprimir(node: Imprimir) {
        line("Imprimir ${pos(node)}")
        indented { node.args.forEach { it.accept(this) } }
    }

    override fun visitLeer(node: Leer) = line("Leer ${pos(node)}")

    override fun visitAsignacionSimple(node: AsignacionSimple) {
        line("AsignacionSimple '${node.nombre.nombre}' ${pos(node)}")
        indented { node.valor.accept(this) }
    }

    override fun visitAsignacionArray(node: AsignacionArray) {
        line("AsignacionArray '${node.nombre.nombre}' ${pos(node)}")
        indented {
            line("indice:"); indented { node.indice.accept(this) }
            line("valor:");  indented { node.valor.accept(this) }
        }
    }

    override fun visitSentenciaExpr(node: SentenciaExpr) {
        line("SentenciaExpr ${pos(node)}")
        indented { node.expr.accept(this) }
    }

    // -----------------------------------------------------------------------
    // Expresiones — binarias
    // -----------------------------------------------------------------------

    private fun binaria(nombre: String, node: ASTNode, izq: Expresion, der: Expresion) {
        line("$nombre ${pos(node)}")
        indented { izq.accept(this); der.accept(this) }
    }

    override fun visitOperacionO(node: OperacionO)           = binaria("OperacionO (||)",   node, node.izq, node.der)
    override fun visitOperacionY(node: OperacionY)           = binaria("OperacionY (&&)",   node, node.izq, node.der)
    override fun visitOperacionDif(node: OperacionDif)       = binaria("OperacionDif (!=)", node, node.izq, node.der)
    override fun visitOperacionIgu(node: OperacionIgu)       = binaria("OperacionIgu (==)", node, node.izq, node.der)
    override fun visitOperacionMen(node: OperacionMen)       = binaria("OperacionMen (<)",  node, node.izq, node.der)
    override fun visitOperacionMenIgu(node: OperacionMenIgu) = binaria("OperacionMenIgu (<=)", node, node.izq, node.der)
    override fun visitOperacionMay(node: OperacionMay)       = binaria("OperacionMay (>)",  node, node.izq, node.der)
    override fun visitOperacionMayIgu(node: OperacionMayIgu) = binaria("OperacionMayIgu (>=)", node, node.izq, node.der)
    override fun visitOperacionSuma(node: OperacionSuma)     = binaria("OperacionSuma (+)", node, node.izq, node.der)
    override fun visitOperacionResta(node: OperacionResta)   = binaria("OperacionResta (-)",node, node.izq, node.der)
    override fun visitOperacionMul(node: OperacionMul)       = binaria("OperacionMul (*)",  node, node.izq, node.der)
    override fun visitOperacionDiv(node: OperacionDiv)       = binaria("OperacionDiv (/)",  node, node.izq, node.der)

    // -----------------------------------------------------------------------
    // Expresiones — unarias
    // -----------------------------------------------------------------------

    override fun visitOperacionAum(node: OperacionAum) {
        line("OperacionAum (++) ${pos(node)}")
        indented { node.objetivo.accept(this) }
    }

    override fun visitOperacionDec(node: OperacionDec) {
        line("OperacionDec (--) ${pos(node)}")
        indented { node.objetivo.accept(this) }
    }

    // -----------------------------------------------------------------------
    // Factores
    // -----------------------------------------------------------------------

    override fun visitLlamadaFuncion(node: LlamadaFuncion) {
        line("LlamadaFuncion '${node.nombre.nombre}' ${pos(node)}")
        indented {
            if (node.args.isEmpty()) line("args: (ninguno)")
            else node.args.forEach { it.accept(this) }
        }
    }

    override fun visitAccesoArray(node: AccesoArray) {
        line("AccesoArray '${node.nombre.nombre}' ${pos(node)}")
        indented { node.indice.accept(this) }
    }

    override fun visitInicializarArray(node: InicializarArray) {
        line("InicializarArray ${pos(node)}")
        indented { node.elementos.forEach { it.accept(this) } }
    }

    override fun visitIdentificador(node: Identificador) = line("Identificador '${node.nombre}' ${pos(node)}")
    override fun visitLiteralEntero(node: LiteralEntero) = line("LiteralEntero ${node.valor} ${pos(node)}")
    override fun visitLiteralCadena(node: LiteralCadena) = line("LiteralCadena \"${node.valor}\" ${pos(node)}")
    override fun visitLiteralBooleano(node: LiteralBooleano) = line("LiteralBooleano ${node.valor} ${pos(node)}")
    override fun visitLiteralNulo(node: LiteralNulo) = line("LiteralNulo ${pos(node)}")
}
