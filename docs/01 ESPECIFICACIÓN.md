# Especificación del Lenguaje CR++

**Curso:** IC-5701 Compiladores e Intérpretes  
**Grupo:** Melcochones  
**Integrantes:**
- Raquel Andrea Gómez Zamora (2022099256)
- Ion Angel Dolanescu Bravo (2022049034)
- Sarah Priscilla Quesada Chaves (2021046027)

---

## 1. Introducción y Propósito

CR++ es un lenguaje de programación imperativo, compilado, de tipado estático y fuerte, diseñado con fines académicos para el curso de Compiladores e Intérpretes. Su propósito es demostrar la construcción formal de un lenguaje de programación completo, incorporando una identidad léxica cultural costarricense como rasgo innovador, sin sacrificar rigor sintáctico ni semántico.

El lenguaje está orientado a la simplicidad conceptual, manteniendo una estructura clara y coherente. Su rasgo distintivo es el uso de palabras clave del léxico coloquial costarricense (como `mae`, `bretee`, `chunche`, `vara`, etc.), evidenciando que la abstracción computacional puede coexistir con el contexto lingüístico de otras culturas.

---

## 2. Modelo Computacional

El lenguaje CR++ es:

- **Compilado:** el código fuente se traduce a un lenguaje objetivo antes de ejecutarse.
- **Imperativo:** la ejecución sigue una secuencia de instrucciones que modifican el estado del programa.
- **Con alcance léxico (estático):** la visibilidad de los identificadores se determina en tiempo de compilación según la estructura del código fuente.
- **Con memoria limitada:** modelo finito realista de implementación.

### Justificación de Turing-completitud

El lenguaje es Turing-completo porque incluye:

1. **Variables mutables** (`vara`): permiten almacenar y modificar estado.
2. **Control condicional** (`mae (...) { ... } tons { ... }`): permite bifurcación de flujo.
3. **Iteración potencialmente infinita** (`bretee`): permite ciclos indefinidos.
4. **Funciones con recursión permitida** (`chunche`): permite computación recursiva.
5. **Memoria estructurada** (variables locales y arreglos `fila_india`): permite representar estructuras de datos.

La combinación de recursión + control condicional + memoria mutable garantiza la capacidad de computar cualquier función computable, dentro de límites físicos de memoria.

---

## 3. Sistema de Tipos

El sistema de tipos es **estático y explícito**:

- Todo identificador debe declarar su tipo explícitamente.
- No existe inferencia de tipos.
- No existen conversiones implícitas entre tipos.
- No se permite la sobrecarga de funciones.

### Tipos Primitivos

| Tipo CR++   | Equivalente | Descripción                            |
|-------------|-------------|----------------------------------------|
| `colones`   | entero      | Número entero (e.g. `0`, `1`, `-25`)  |
| `diay`      | booleano    | Valor lógico (`diay_si` o `diay_no`)  |
| `labia`     | string      | Cadena de texto entre comillas dobles  |
| `ni_papa`   | void / null | Ausencia de valor o tipo vacío         |

> **Nota:** CR++ no soporta números de punto flotante. Solo existe el tipo entero `colones`.

### Valores Literales

- **LIT_ENTERO:** `[0-9]+` — ejemplos válidos: `0`, `1`, `1234`. Inválidos: `a`, `12x`.
- **LIT_STRING:** `"[^"]*"` — ejemplos válidos: `"hola mundo"`, `"5+3=b"`, `""`. Inválidos: `hola mundo`.
- **Booleanos:** `diay_si` (verdadero), `diay_no` (falso).
- **Nulo/vacío:** `ni_papa` representa ausencia de valor.

---

## 4. Arreglos (`fila_india`)

Los arreglos en CR++ se declaran con el tipo `fila_india(T)`, donde `T` es el tipo de los elementos. Son estructuras de tamaño fijo y homogéneas: todos sus elementos deben ser del mismo tipo.

### Declaración e inicialización

Todo arreglo **debe inicializarse en el momento de su declaración**. No se permite declarar un arreglo sin valores iniciales:

```
vara fila_india(colones) edades = [1, 2, 3];
vara fila_india(diay) flags = [diay_si, diay_no, diay_si];
```

### Índices

Los índices comienzan en `0`. Si se intenta acceder a un índice igual o mayor a la longitud del arreglo, se produce un error en tiempo de ejecución:

```
Alerta Jaguarsh: índice fuera de rango
```

### Acceso a elementos

El acceso a un elemento se realiza con la notación de paréntesis cuadrados:

```
vara colones x = edades[0];
edades[1] = 99;
```

### Arreglos multidimensionales

Se permiten mediante la notación anidada `fila_india(fila_india(T))`. La inicialización anida corchetes y el acceso encadena corchetes:

```
vara fila_india(fila_india(colones)) matriz = [[1, 2], [3, 4]];
vara colones x = matriz[0][1];
```

### Paso a funciones

Los arreglos se pasan **por referencia** a las funciones. Cualquier modificación dentro de la función afecta al arreglo original.

---

## 5. Variables y Constantes

### Declaración de Variables

```
vara colones edad = 15;
```

- Alcance léxico: las variables viven únicamente dentro de su bloque `{ }`.
- Se permite sombreado de variables de bloques exteriores.
- No existen variables globales.

### Declaración de Constantes

```
de_fijo colones MAX = 10;
```

- Las constantes deben inicializarse en su declaración y no pueden reasignarse.

### Identificadores

Regex: `[a-zA-ZÁÉÍÓÚáéíóúÜüÑñ_][a-zA-ZÁÉÍÓÚáéíóúÜüÑñ0-9_]*`

Las keywords que en español llevan tilde se reconocen con o sin ella (`tómela` = `tomela`, `me_la_comí` = `me_la_comi`, `diay_sí` = `diay_si`).

Ejemplos válidos: `factorial`, `contador`, `MAX`, `mi_variable1`, `índice`

---

## 6. Funciones

### Sintaxis General

```
chunche <tipo_retorno> <nombre>(<tipo> <param>, ...) {
    <bloque>
} : <tipo_retorno>
```

### Propiedades

- Permiten recursión.
- No son ciudadanos de primera clase.
- Se permiten funciones anidadas.
- No se permite la sobrecarga.
- `tomela` (return) es obligatorio incluso si el tipo de retorno es `ni_papa`.

### Punto de Entrada del Programa

Todo archivo CR++ debe contener una función denominada `chante`, sin parámetros y con retorno `ni_papa`:

```
chunche ni_papa chante() {
    ...
} : ni_papa
```

### Ejemplo

```
chunche colones factorial(colones n) {
    mae (n == 0) {
        tomela 1;
    } tons {
        tomela n * factorial(n - 1);
    }
} : colones
```

---

## 7. Control de Flujo

### Condicional

```
mae (<condicion>) {
    <bloque_verdadero>
} tons {
    <bloque_falso>
}
```

La cláusula `tons` es opcional.

### Ciclo Indefinido

```
bretee (<condicion>) {
    <bloque>
}
```

### Control Interno del Ciclo

Solo usable dentro de `bretee`:
- `jaleas` → equivalente a `break`.
- `dele_dele` → equivalente a `continue`.

---

## 8. Operadores

La precedencia sigue el modelo del lenguaje C.

| Categoría    | Operadores                       |
|--------------|----------------------------------|
| Aritméticos  | `+`, `-`, `*`, `/`               |
| Incremento   | `++`, `--`                       |
| Asignación   | `=`                              |
| Relacionales | `==`, `!=`, `<`, `>`, `<=`, `>=` |
| Lógicos      | `&&`, `||`, `!`                  |

---

## 9. Entrada / Salida

### Imprimir (`miau`)

Acepta múltiples argumentos de distintos tipos:

```
miau("Resultado: ", resultado);
```

### Leer (`me_la_comí`)

Lee desde stdin y retorna `labia`:

```
vara labia entrada = me_la_comí();
```

---

## 10. Manejo de Errores

### En compilación

```
Jaguarsh: <descripción del error>
```

### En ejecución

```
Alerta Jaguarsh: <explicación del error>
```

No existe manejo de excepciones en tiempo de ejecución.

---

## 11. Palabras Reservadas

Los identificadores y keywords en CR++ aceptan caracteres con o sin tilde. Esto aplica a las keywords que en español se escriben con tilde: el compilador reconoce ambas formas como el mismo token. Por ejemplo, `tómela` y `tomela` son equivalentes; lo mismo aplica a `me_la_comí` / `me_la_comi`, `diay_sí` / `diay_si`.

Regex de identificadores/keywords: `[a-zA-ZÁÉÍÓÚáéíóúÜüÑñ_][a-zA-ZÁÉÍÓÚáéíóúÜüÑñ0-9_]*`

| Token         | Keyword (con tilde)  | Keyword (sin tilde) | Descripción                 |
|---------------|----------------------|---------------------|-----------------------------|
| `VARIABLE`    | `vara`               | `vara`              | Declaración de variable     |
| `CONSTANTE`   | `de_fijo`            | `de_fijo`           | Declaración de constante    |
| `CICLO_INDEF` | `bretee`             | `bretee`            | Ciclo while                 |
| `IMPRIMIR`    | `miau`               | `miau`              | Imprimir en consola         |
| `RECIBIR`     | `me_la_comí`         | `me_la_comi`        | Leer de stdin               |
| `RETORNAR`    | `tómela`             | `tomela`            | Retorno de función          |
| `FUNCION`     | `chunche`            | `chunche`           | Declaración de función      |
| `CONDICIONAL` | `mae`                | `mae`               | Condicional if              |
| `SINO`        | `tons`               | `tons`              | Cláusula else               |
| `ENTERO`      | `colones`            | `colones`           | Tipo entero                 |
| `BOOLEANO`    | `diay`               | `diay`              | Tipo booleano               |
| `VERDADERO`   | `diay_sí`            | `diay_si`           | Literal verdadero           |
| `FALSO`       | `diay_no`            | `diay_no`           | Literal falso               |
| `CADENA`      | `labia`              | `labia`             | Tipo cadena de texto        |
| `VACIO`       | `ni_papa`            | `ni_papa`           | Tipo/valor nulo o void      |
| `BREAK`       | `jaleas`             | `jaleas`            | Interrumpir ciclo           |
| `CONTINUE`    | `dele_dele`          | `dele_dele`         | Continuar ciclo             |

---

## 12. Extensión de Archivos

Los archivos de código fuente CR++ utilizan la extensión `.crpp`.
