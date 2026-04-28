package edu.ic5701.scanner

import edu.ic5701.tokens.Token

interface Scanner {
    fun scanAll(): List<Token>
}