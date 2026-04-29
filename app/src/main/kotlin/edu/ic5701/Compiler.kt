package edu.ic5701

import edu.ic5701.scanner.Scanner
import edu.ic5701.scanner.ScannerImpl
import edu.ic5701.tokens.TokenType
import java.io.File
import kotlin.system.exitProcess

/**
 * Punto de entrada del compilador CR++.
 *
 * Uso:
 *   ./compiler <ruta_al_archivo.crpp>
 *
 * El programa imprime en stdout la lista de tokens reconocidos.
 * Los errores léxicos se reportan en stderr con el formato:
 *   Jaguarsh (línea N, columna M): <descripción>
 */
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("Uso: compiler <archivo.crpp>")
            exitProcess(1)
        }

        val path = args[0]
        val file = File(path)

        if (!file.exists()) {
            System.err.println("Jaguarsh: no se encontró el archivo '$path'")
            exitProcess(1)
        }

        if (!file.name.endsWith(".crpp")) {
            System.err.println("Advertencia: el archivo '$path' no tiene extensión .crpp")
        }

        val source = file.readText(Charsets.UTF_8)
        val scanner: Scanner = ScannerImpl(source)
        val tokens = scanner.scanAll()

        // ----------------------------------------------------------------
        // Imprimir tokens reconocidos (excluyendo EOF si no hay errores)
        // ----------------------------------------------------------------
        println("=".repeat(60))
        println("  Tokens reconocidos — ${file.name}")
        println("=".repeat(60))

        val nonEof = tokens.filter { it.type != TokenType.EOF }
        val colW = 22  // ancho de la columna de tipo

        println("%-${colW}s  %-6s  %-6s  %s".format("TIPO", "LÍNEA", "COL", "LEXEMA"))
        println("-".repeat(60))

        for (tok in nonEof) {
            println(
                "%-${colW}s  %-6d  %-6d  %s".format(
                    tok.type.name,
                    tok.line,
                    tok.column,
                    tok.lexeme
                )
            )
        }

        println("-".repeat(60))
        println("Total: ${nonEof.size} token(s)")

        // ----------------------------------------------------------------
        // Resumen de errores
        // ----------------------------------------------------------------
        if (scanner.errors.isEmpty()) {
            println("\n✓ Análisis léxico completado sin errores.")
        } else {
            println("\n✗ Se encontraron ${scanner.errors.size} error(es) léxico(s).")
            System.exit(2)
        }
    }
}