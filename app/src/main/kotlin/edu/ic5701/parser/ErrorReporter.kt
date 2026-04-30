package edu.ic5701.parser

import edu.ic5701.tokens.TokenType

/**
 * centraliza el registro y reporte de errores sintacticos.
 * separa la responsabilidad de manejo de errores del parsing propiamente dicho,
 * siguiendo el principio de responsabilidad unica.
 */
internal class ErrorReporter(private val stream: TokenStream) {

    private val _errors: MutableList<String> = mutableListOf()

    /**
     * lista publica de errores registrados durante el analisis.
     */
    val errors: List<String> get() = _errors

    /**
     * registra un mensaje de error en la lista y lo imprime en stderr.
     * retorna una ParseException lista para ser lanzada por el sitio que llama.
     * separar el registro del lanzamiento permite que el parser controle
     * exactamente donde interrumpe el flujo.
     */
    fun reportError(message: String): ParseException {
        val formatted = "Jaguarsh sintactico: $message"
        _errors.add(formatted)
        System.err.println(formatted)
        return ParseException(formatted)
    }

    /**
     * construye un mensaje de error indicando que se esperaba un tipo de token
     * pero se encontro otro, incluyendo la posicion en el archivo fuente.
     */
    fun expectedError(expected: TokenType): ParseException {
        val found = stream.peek()
        return reportError(
            "se esperaba '${expected.name}' pero se encontro '${found.lexeme}' " +
            "(${found.type.name}) en linea ${found.line}, columna ${found.column}"
        )
    }

    /**
     * recuperacion de errores por modo panico.
     * descarta tokens hasta encontrar un punto de sincronizacion seguro
     * (FIN_SENTENCIA o CORCHETE_CERRADO) o hasta llegar al EOF.
     */
    fun synchronize() {
        val syncTokens = setOf(
            TokenType.FIN_SENTENCIA,
            TokenType.CORCHETE_CERRADO
        )
        while (!stream.isAtEnd()) {
            if (stream.peek().type in syncTokens) {
                stream.advance()
                return
            }
            stream.advance()
        }
    }
}