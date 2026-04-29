package edu.ic5701.scanner

import edu.ic5701.tokens.Token

interface Scanner {
    val errors: MutableList<String>

    fun scanAll(): List<Token>
}