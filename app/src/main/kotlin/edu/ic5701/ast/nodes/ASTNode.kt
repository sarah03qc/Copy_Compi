package edu.ic5701.ast.nodes

/**
 * Interfaz base para todos los nodos del AST de CR++.
 *
 * [inicio] e [fin] corresponden al índice del token de apertura y cierre
 * en la lista de tokens producida por el Scanner (N = -1 cuando no aplica
 * o cuando el nodo es sintético).
 */
sealed interface ASTNode {
    val inicio: Int
    val fin: Int
}