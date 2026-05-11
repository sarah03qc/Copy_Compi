package edu.ic5701.ast
import edu.ic5701.ast.nodes.*

/**
 * Despacha [visitor] al método `visit*` correcto según el tipo concreto
 * del nodo receptor.
 *
 * Al usar una función de extensión con `when` exhaustivo sobre la sealed
 * hierarchy, el compilador de Kotlin garantiza que ningún nodo quede sin
 * despachar: si se agrega un nuevo subtipo al AST, este archivo no compilará
 * hasta que se agregue la rama correspondiente.
 *
 * Uso:
 * ```kotlin
 * val resultado = miNodo.accept(miVisitor)
 * ```
 */
fun <R> ASTNode.accept(visitor: ASTVisitor<R>): R = when (this) {

    // -----------------------------------------------------------------------
    // Raíz
    // -----------------------------------------------------------------------
    is Programa            -> visitor.visitPrograma(this)

    // -----------------------------------------------------------------------
    // Tipos
    // -----------------------------------------------------------------------
    is TipoColones         -> visitor.visitTipoColones(this)
    is TipoDiay            -> visitor.visitTipoDiay(this)
    is TipoLabia           -> visitor.visitTipoLabia(this)
    is TipoNiPapa          -> visitor.visitTipoNiPapa(this)
    is TipoArray           -> visitor.visitTipoArray(this)

    // -----------------------------------------------------------------------
    // Declaraciones
    // -----------------------------------------------------------------------
    is VariableDecl        -> visitor.visitVariableDecl(this)
    is ConstanteDecl       -> visitor.visitConstanteDecl(this)
    is FuncionDecl         -> visitor.visitFuncionDecl(this)
    is Parametro           -> visitor.visitParametro(this)

    // -----------------------------------------------------------------------
    // Sentencias
    // -----------------------------------------------------------------------
    is Bloque              -> visitor.visitBloque(this)
    is Retorno             -> visitor.visitRetorno(this)
    is Condicional         -> visitor.visitCondicional(this)
    is Ciclo               -> visitor.visitCiclo(this)
    is Ruptura             -> visitor.visitRuptura(this)
    is Continuar           -> visitor.visitContinuar(this)
    is Imprimir            -> visitor.visitImprimir(this)
    is Leer                -> visitor.visitLeer(this)
    is AsignacionSimple    -> visitor.visitAsignacionSimple(this)
    is AsignacionArray     -> visitor.visitAsignacionArray(this)
    is SentenciaExpr       -> visitor.visitSentenciaExpr(this)

    // -----------------------------------------------------------------------
    // Expresiones — operaciones binarias
    // -----------------------------------------------------------------------
    is OperacionO          -> visitor.visitOperacionO(this)
    is OperacionY          -> visitor.visitOperacionY(this)
    is OperacionDif        -> visitor.visitOperacionDif(this)
    is OperacionIgu        -> visitor.visitOperacionIgu(this)
    is OperacionMen        -> visitor.visitOperacionMen(this)
    is OperacionMenIgu     -> visitor.visitOperacionMenIgu(this)
    is OperacionMay        -> visitor.visitOperacionMay(this)
    is OperacionMayIgu     -> visitor.visitOperacionMayIgu(this)
    is OperacionSuma       -> visitor.visitOperacionSuma(this)
    is OperacionResta      -> visitor.visitOperacionResta(this)
    is OperacionMul        -> visitor.visitOperacionMul(this)
    is OperacionDiv        -> visitor.visitOperacionDiv(this)

    // -----------------------------------------------------------------------
    // Expresiones — operaciones unarias
    // -----------------------------------------------------------------------
    is OperacionAum        -> visitor.visitOperacionAum(this)
    is OperacionDec        -> visitor.visitOperacionDec(this)

    // -----------------------------------------------------------------------
    // Factores
    // -----------------------------------------------------------------------
    is LlamadaFuncion      -> visitor.visitLlamadaFuncion(this)
    is AccesoArray         -> visitor.visitAccesoArray(this)
    is InicializarArray    -> visitor.visitInicializarArray(this)
    is Identificador       -> visitor.visitIdentificador(this)
    is LiteralEntero       -> visitor.visitLiteralEntero(this)
    is LiteralCadena       -> visitor.visitLiteralCadena(this)
    is LiteralBooleano     -> visitor.visitLiteralBooleano(this)
    is LiteralNulo         -> visitor.visitLiteralNulo(this)
}
