package edu.ic5701.ast

import edu.ic5701.ast.nodes.*
/**
 * Visitor genérico para el AST de CR++.
 *
 * [R] es el tipo de retorno de cada visita. Usá [Unit] si no necesitás
 * retornar nada (p. ej. un printer), o [String] para un pretty-printer,
 * o cualquier tipo para un evaluador/generador de código.
 *
 * Cada método tiene una implementación por defecto que lanza
 * [UnsupportedOperationException], de modo que una implementación parcial
 * falla ruidosamente en lugar de silenciosamente.
 */
interface ASTVisitor<R> {

    // -----------------------------------------------------------------------
    // Raíz
    // -----------------------------------------------------------------------
    fun visitPrograma(node: Programa): R

    // -----------------------------------------------------------------------
    // Tipos
    // -----------------------------------------------------------------------
    fun visitTipoColones(node: TipoColones): R
    fun visitTipoDiay(node: TipoDiay): R
    fun visitTipoLabia(node: TipoLabia): R
    fun visitTipoNiPapa(node: TipoNiPapa): R
    fun visitTipoArray(node: TipoArray): R

    // -----------------------------------------------------------------------
    // Declaraciones
    // -----------------------------------------------------------------------
    fun visitVariableDecl(node: VariableDecl): R
    fun visitConstanteDecl(node: ConstanteDecl): R
    fun visitFuncionDecl(node: FuncionDecl): R
    fun visitParametro(node: Parametro): R

    // -----------------------------------------------------------------------
    // Sentencias
    // -----------------------------------------------------------------------
    fun visitBloque(node: Bloque): R
    fun visitRetorno(node: Retorno): R
    fun visitCondicional(node: Condicional): R
    fun visitCiclo(node: Ciclo): R
    fun visitRuptura(node: Ruptura): R
    fun visitContinuar(node: Continuar): R
    fun visitImprimir(node: Imprimir): R
    fun visitLeer(node: Leer): R
    fun visitAsignacionSimple(node: AsignacionSimple): R
    fun visitAsignacionArray(node: AsignacionArray): R
    fun visitSentenciaExpr(node: SentenciaExpr): R

    // -----------------------------------------------------------------------
    // Expresiones / operaciones binarias
    // -----------------------------------------------------------------------
    fun visitOperacionO(node: OperacionO): R
    fun visitOperacionY(node: OperacionY): R
    fun visitOperacionDif(node: OperacionDif): R
    fun visitOperacionIgu(node: OperacionIgu): R
    fun visitOperacionMen(node: OperacionMen): R
    fun visitOperacionMenIgu(node: OperacionMenIgu): R
    fun visitOperacionMay(node: OperacionMay): R
    fun visitOperacionMayIgu(node: OperacionMayIgu): R
    fun visitOperacionSuma(node: OperacionSuma): R
    fun visitOperacionResta(node: OperacionResta): R
    fun visitOperacionMul(node: OperacionMul): R
    fun visitOperacionDiv(node: OperacionDiv): R

    // -----------------------------------------------------------------------
    // Expresiones / operaciones unarias
    // -----------------------------------------------------------------------
    fun visitOperacionAum(node: OperacionAum): R
    fun visitOperacionDec(node: OperacionDec): R

    // -----------------------------------------------------------------------
    // Factores
    // -----------------------------------------------------------------------
    fun visitLlamadaFuncion(node: LlamadaFuncion): R
    fun visitAccesoArray(node: AccesoArray): R
    fun visitInicializarArray(node: InicializarArray): R
    fun visitIdentificador(node: Identificador): R
    fun visitLiteralEntero(node: LiteralEntero): R
    fun visitLiteralCadena(node: LiteralCadena): R
    fun visitLiteralBooleano(node: LiteralBooleano): R
    fun visitLiteralNulo(node: LiteralNulo): R
}
