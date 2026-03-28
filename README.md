# Proyecto
IC-5701 Compiladores e intérpretes  
Proyecto de implementación de un compilador

# Objetivo general

Diseñar un lenguaje de programación con características mínimas para ser Turing-completo e implementar un compilador para este lenguaje.

# Objetivos de aprendizaje
* Diseñar y especificar un lenguaje de
  programación (VI)
* Realizar el análisis de los
  componentes léxicos en expresiones
  regulares para un lenguaje de
  programación (IV)
* Diseñar un algoritmo para escanear
  los componentes léxicos de un
  lenguaje de programación (VI)
* Diseñar un árbol de sintaxis para un
  lenguaje de programación (VI)
* Construir un analizador sintáctico
  para un lenguaje de programación
  (VI)
* Especificar reglas de chequeo de
  tipos y resolución de identificadores
  para un lenguaje de programación
  (VI)
* Construir un analizador semántico
  para un lenguaje de programación
  (VI)
* Especificar las reglas de traducción
  de un lenguaje fuente a un lenguaje
* Construir un traductor de un
  lenguaje fuente a un lenguaje
  objetivo (VI)


# Tareas
1. Documentar especificación informal del lenguaje en `docs/01 ESPECIFICACION.md`. Utilizando su lenguaje de programación, agregar una implementación recursiva de pila de la función factorial en `samples/factorial`, y una implementación iterativa de ordenamiento burbuja en `samples/burbuja`. Si su lenguaje soporta hileras implementar una función palíndrome en `samples/palíndrome`. Si su lenguaje soporta números flotantes implementar la función de raíz cuadrada usando el método de Newton-Raphson en `samples/raízc`. El compilador deberá poder procesar estos archivos de ejemplo.
2. Documentar diagrama de transiciones para el analizador léxico `docs/02 LEXICO.md`. Instalar el plugin de PlantUML en IntelliJ IDEA.
3. Implementar un analizador léxico con base en el autómata definido en `docs/02 LEXICO.md` en el paquete `edu.ic5701.scanner`. El scanner debe reportar errores léxicos. Crear un `main` en la clase `edu.ic5701.Compiler` que invoque el scanner, reciba la ruta al archivo de código fuente en línea de comando e imprima en pantalla la lista de tokens reconocidos (incluyendo tipo, lexema y demás metadatos).
4. Documentar la gramática del lenguaje en `docs/02 GRAMATICA.md`
5. Implementar un parser de descenso recursivo para la gramática en `docs/02 GRAMATICA.md` en el paquete `edu.ic5701.parser`. Asumimos que ya hemos demostrado que la gramática es LL(1). El parser debe reportar errores sintácticos o reportar que el programa no presentó errores. Modificar `edu.ic5701.Compiler.main` para que ahora invoque al parser e indique si hubo errores léxicos o sintácticos, o si más bien el archivo fuente es válido. 
6. Implementar las estructuras de datos del AST en el paquete `edu.ic5701.ast`.
7. Modificar el parser para que retorne un AST correspondiente al código reconocido o reporte errores sintácticos. Modificar `edu.ic5701.Compiler.main` para que imprima en stdout el AST producido por el parser.
8. Documentar la especificación semántica operativa del lenguaje en `docs/04 SEMANTICA.md`.
9. Implementar un analizador semántico en el paquete `edu.ic5701.sem`, este debe reportar errores semánticos.
10. Documentar las reglas de traducción del lenguaje fuente al lenguaje objetivo en `docs/05 TRADUCCIÓN.md`.
11. Implementar un generador de código para el lenguaje siguiendo las reglas en `docs/05 TRADUCCIÓN.md` en el paquete `edu.ic5701.gen`. Modificar `edu.ic5701.Compiler.main` para que produzca un archivo de código ejecutable que tenga el mismo nombre que el archivo fuente recibido con extensión `.out`.

