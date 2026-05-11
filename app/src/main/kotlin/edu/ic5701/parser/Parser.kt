package edu.ic5701.parser
import edu.ic5701.ast.nodes.Programa

interface Parser {
    val errors: List<String>
    fun parse(): Programa?
}