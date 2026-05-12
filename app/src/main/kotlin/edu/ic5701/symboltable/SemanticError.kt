package edu.ic5701.symboltable

/**
 * Error semántico detectado durante el análisis de la tabla de símbolos.
 *
 * [mensaje] describe el problema en términos del lenguaje CR++.
 * [nombreSimbolo] es el identificador involucrado (para mensajes de error claros).
 */
class SemanticError(
    val mensaje: String,
    val nombreSimbolo: String? = null
) : Exception(
    if (nombreSimbolo != null) "Error semántico [$nombreSimbolo]: $mensaje"
    else "Error semántico: $mensaje"
)