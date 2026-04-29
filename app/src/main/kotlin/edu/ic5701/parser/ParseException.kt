package edu.ic5701.parser

/**
 * excepcion interna lanzada cuando el parser encuentra un token inesperado.
 * se usa para implementar recuperacion de errores por modo panico: al atraparla,
 * el parser descarta tokens hasta encontrar un punto de sincronizacion seguro
 * (por ejemplo, fin de sentencia o llave de cierre) y retoma el analisis desde ahi.
 *
 * no debe propagarse fuera de ParserImpl.
 */
internal class ParseException(message: String) : Exception(message)