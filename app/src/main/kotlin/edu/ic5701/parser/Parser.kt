package edu.ic5701.parser

interface Parser {
    val errors: List<String>
    fun parse(): Boolean
}