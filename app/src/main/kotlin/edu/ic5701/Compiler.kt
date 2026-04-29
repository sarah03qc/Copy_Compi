package edu.ic5701

import edu.ic5701.parser.ParserImpl
import edu.ic5701.scanner.Scanner
import edu.ic5701.scanner.ScannerImpl
import edu.ic5701.tokens.TokenType
import java.io.File
import kotlin.system.exitProcess

/**
 * punto de entrada del compilador CR++.
 *
 * uso:
 *   ./compiler <ruta_al_archivo.crpp>
 *
 * el programa imprime en stdout la lista de tokens reconocidos y el resultado
 * del analisis sintactico. los errores lexicos y sintacticos se reportan en
 * stderr con el formato:
 *   Jaguarsh (linea N, columna M): descripcion
 */
object Compiler {
    @JvmStatic
    fun main(args: Array<String>) {
        if (args.isEmpty()) {
            System.err.println("uso: compiler <archivo.crpp>")
            exitProcess(1)
        }

        val path = args[0]
        val file = File(path)

        if (!file.exists()) {
            System.err.println("Jaguarsh: no se encontro el archivo '$path'")
            exitProcess(1)
        }

        if (!file.name.endsWith(".crpp")) {
            System.err.println("advertencia: el archivo '$path' no tiene extension .crpp")
        }

        val source = file.readText(Charsets.UTF_8)

        // analisis lexico
        val scanner: Scanner = ScannerImpl(source)
        val tokens = scanner.scanAll()

        println("=".repeat(60))
        println("  tokens reconocidos: ${file.name}")
        println("=".repeat(60))

        val nonEof = tokens.filter { it.type != TokenType.EOF }
        val colW = 22

        println("%-${colW}s  %-6s  %-6s  %s".format("tipo", "linea", "col", "lexema"))
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
        println("total: ${nonEof.size} token(s)")

        val hayErroresLexicos = scanner.errors.isNotEmpty()

        // analisis sintactico
        println("\n" + "=".repeat(60))
        println("  analisis sintactico: ${file.name}")
        println("=".repeat(60))

        val parser = ParserImpl(tokens)
        val esValido = parser.parse()

        val hayErroresSintacticos = parser.errors.isNotEmpty()

        // resumen final
        println("\n" + "=".repeat(60))
        println("  resumen")
        println("=".repeat(60))

        when {
            hayErroresLexicos && hayErroresSintacticos -> {
                println("se encontraron errores lexicos y sintacticos.")
                exitProcess(2)
            }
            hayErroresLexicos -> {
                println("se encontraron ${scanner.errors.size} error(es) lexico(s).")
                exitProcess(2)
            }
            hayErroresSintacticos -> {
                println("se encontraron ${parser.errors.size} error(es) sintactico(s).")
                exitProcess(2)
            }
            else -> {
                println("el archivo '${file.name}' es sintacticamente valido.")
                exitProcess(0)
            }
        }
    }
}