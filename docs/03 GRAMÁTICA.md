# Gramática del Lenguaje CR++

**Curso:** IC-5701 Compiladores e Intérpretes  
**Grupo:** Melcochones  
**Integrantes:**
- Raquel Andrea Gómez Zamora (2022099256)
- Ion Angel Dolanescu Bravo (2022049034)
- Sarah Priscilla Quesada Chaves (2021046027)

---

## 1. Convenciones de Notación

Se utiliza notación BNF extendida con las siguientes convenciones:

- Los **no-terminales** se escriben en `MAYÚSCULAS_CON_GUIÓN`.
- Los **terminales** (tokens) se escriben en `minúsculas` o entre comillas.
- `ε` denota la producción vacía.
- `A → α | β` significa que `A` puede derivar `α` o `β`.
- Las producciones auxiliares con prima (`A'`) eliminan la recursión por la izquierda.

---

## 2. Gramática Completa en Notación BNF

### 2.1 Programa

Un programa CR++ es una secuencia de declaraciones de funciones a nivel global. No se permiten variables globales; toda declaración de variable/constante ocurre dentro del cuerpo de una función.

```
PROGRAMA        → DECL_FUNC PROGRAMA'
PROGRAMA'       → DECL_FUNC PROGRAMA' | ε
```

> **Corrección del Taller 03:** La producción original `PROGRAMA → DECLARACION PROGRAMA | ε` presentaba un conflicto porque `FIRST(PROGRAMA) ∩ FOLLOW(PROGRAMA) ≠ ∅` cuando se usaba `ε`. Al restringir el nivel global **únicamente a declaraciones de función** (encabezadas por `chunche`), el único token inicial posible es `chunche`, y `FOLLOW(PROGRAMA')` = {`EOF`}, lo que elimina el conflicto.

---

### 2.2 Declaración de Función

```
DECL_FUNC       → chunche TIPO IDENT ( PARAMS ) BLOQUE : TIPO ;
```

> La firma termina con `: TIPO ;` conforme a la especificación del lenguaje.

---

### 2.3 Parámetros

```
PARAMS          → PARAM PARAMS' | ε

PARAMS'         → , PARAM PARAMS' | ε

PARAM           → TIPO IDENT
```

---

### 2.4 Tipos

```
TIPO            → colones | diay | labia | ni_papa | fila_india ( TIPO )
```

---

### 2.5 Bloque y Sentencias

```
BLOQUE          → { SENTENCIAS }

SENTENCIAS      → SENTENCIA SENTENCIAS'

SENTENCIAS'     → SENTENCIA SENTENCIAS' | ε
```

---

### 2.6 Sentencia

```
SENTENCIA       → DECL_VAR | DECL_CONST | SENT_RETORNO | SENT_COND | SENT_CICLO | SENT_PRINT | SENT_BREAK | SENT_CONTINUE | SENT_EXPR
```

> **Nota:** `me_la_comí` (RECIBIR) ya no genera una sentencia propia; aparece como factor dentro de una expresión (ver 2.11). Así se elimina `SENT_READ`.

---

### 2.7 Declaraciones de Variables y Constantes

```
DECL_VAR        → vara TIPO IDENT = EXPRESION ;

DECL_CONST      → de_fijo TIPO IDENT = EXPRESION ;
```

---

### 2.8 Sentencias de Control

```
SENT_RETORNO    → tomela EXPRESION ;

SENT_COND       → mae ( EXPRESION ) BLOQUE SINO_OPC

SINO_OPC        → tons BLOQUE | ε

SENT_CICLO      → bretee ( EXPRESION ) BLOQUE

SENT_BREAK      → jaleas ;

SENT_CONTINUE   → dele_dele ;
```

---

### 2.9 Sentencia de Impresión

```
SENT_PRINT      → miau ( ARGS ) ;
```

> `miau` acepta múltiples argumentos separados por coma (varargs a nivel gramatical; se trata como construcción sintáctica especial, no como función normal).

---

### 2.10 Sentencia de Expresión (asignaciones e incrementos)

Una sentencia de expresión permite asignaciones simples, asignación a elementos de arreglo, y el uso de `++`/`--` como sentencias independientes.

```
SENT_EXPR       → IDENT SENT_IDENT_COLA ;

SENT_IDENT_COLA → = EXPRESION ; | ( ARGS ) ; | [ EXPRESION ] SENT_INDEX_COLA | ++ ; | -- ;

SENT_INDEX_COLA → = EXPRESION ; | [ EXPRESION ] SENT_INDEX_COLA
```

---

### 2.11 Argumentos

```
ARGS            → EXPRESION ARGS' | ε

ARGS'           → , EXPRESION ARGS' | ε
```

---

### 2.12 Expresiones (con prioridad y jerarquía)

La jerarquía de operadores, de menor a mayor precedencia:

```
EXPRESION       → OR_EXPR

OR_EXPR         → AND_EXPR OR_EXPR'

OR_EXPR'        → || AND_EXPR OR_EXPR' | ε

AND_EXPR        → EQUAL_EXPR AND_EXPR'

AND_EXPR'       → && EQUAL_EXPR AND_EXPR' | ε

EQUAL_EXPR      → RELAC_EXPR EQUAL_EXPR'

EQUAL_EXPR'     → == RELAC_EXPR EQUAL_EXPR' | != RELAC_EXPR EQUAL_EXPR' | ε

RELAC_EXPR      → ARIT_EXPR RELAC_EXPR'

RELAC_EXPR'     → < ARIT_EXPR RELAC_EXPR' | <= ARIT_EXPR RELAC_EXPR' | > ARIT_EXPR RELAC_EXPR' | >= ARIT_EXPR RELAC_EXPR' | ε

ARIT_EXPR       → TERMINO ARIT_EXPR'

ARIT_EXPR'      → + TERMINO ARIT_EXPR' | - TERMINO ARIT_EXPR' | ε

TERMINO         → FACTOR TERMINO'

TERMINO'        → * FACTOR TERMINO' | / FACTOR TERMINO' | ε
```

---

### 2.13 Factores

```
FACTOR          → ! FACTOR | - FACTOR | FACTOR_BASE FACTOR'

FACTOR_BASE     → ( EXPRESION ) | [ LISTA_ELEMENTOS ] | IDENT | LIT_ENTERO | LIT_STRING | diay_si | diay_no | me_la_comi ( )

LISTA_ELEMENTOS  → EXPRESION LISTA_ELEMENTOS' | ε

LISTA_ELEMENTOS' → , EXPRESION LISTA_ELEMENTOS' | ε

FACTOR'         → ++ | -- | ( ARGS ) | [ EXPRESION ] FACTOR_INDEX | ε

FACTOR_INDEX    → [ EXPRESION ] FACTOR_INDEX | ε
```

> **Corrección del Taller 03:** Se añaden los operadores unarios `!` (NOT lógico) y `-` (NEG aritmético) como prefijos de `FACTOR`. Esto permite expresiones como `!condicion` y `-n`.

> **Nota sobre `FACTOR'`:** Cuando se ve `(` después de un `IDENT`, se trata de una llamada a función `f(ARGS)`. Cuando se ve `[` después de un `IDENT`, se trata de un acceso a elemento de arreglo `arr[i]`, posiblemente encadenado para arreglos multidimensionales `matriz[i][j]`. La distinción entre función y arreglo en el caso de `(` se delega al **analizador semántico**.

---

## 3. Tabla de Tokens de Referencia

### Keywords

| Token | Lexema(s) |
|---|---|
| `FUNCION` | `chunche` |
| `VARIABLE` | `vara` |
| `CONSTANTE` | `de_fijo` |
| `ENTERO` | `colones` |
| `BOOLEANO` | `diay` |
| `CADENA` | `labia` |
| `VACIO` | `ni_papa` |
| `ARREGLO` | `fila_india` |
| `CONDICIONAL` | `mae` |
| `SINO` | `tons` |
| `CICLO_INDEF` | `bretee` |
| `RETORNAR` | `tomela` / `tómela` |
| `IMPRIMIR` | `miau` |
| `RECIBIR` | `me_la_comi` / `me_la_comí` |
| `BREAK` | `jaleas` |
| `CONTINUE` | `dele_dele` |
| `VERDADERO` | `diay_si` / `diay_sí` |
| `FALSO` | `diay_no` |

### Identificadores y Literales

| Token | Lexema(s) — patrón regex |
|---|---|
| `IDENT` | `[a-zA-ZÁÉÍÓÚáéíóúÜüÑñ_][a-zA-ZÁÉÍÓÚáéíóúÜüÑñ0-9_]*` |
| `LIT_ENTERO` | `[0-9]+` |
| `LIT_STRING` | `"[^"]*"` |

### Operadores

| Token | Lexema |
|---|---|
| `OP_SUMA` | `+` |
| `OP_RESTA` | `-` |
| `OP_MUL` | `*` |
| `OP_DIV` | `/` |
| `OP_INC` | `++` |
| `OP_DEC` | `--` |
| `OP_ASIG` | `=` |
| `OP_IGUALD` | `==` |
| `OP_DIFE` | `!=` |
| `OP_MAYOR` | `>` |
| `OP_MENOR` | `<` |
| `OP_MAYOR_IG` | `>=` |
| `OP_MENOR_IG` | `<=` |
| `OP_Y` | `&&` |
| `OP_O` | `\|\|` |
| `OP_NOT` | `!` |

### Delimitadores

| Token | Lexema |
|---|---|
| `PAREN_ABIERTO` | `(` |
| `PAREN_CERRADO` | `)` |
| `LLAVE_ABIERTA` | `{` |
| `LLAVE_CERRADA` | `}` |
| `CORCHETE_ABIERTO` | `[` |
| `CORCHETE_CERRADO` | `]` |
| `FIN_SENTENCIA` | `;` |
| `SEPARAR` | `,` |
| `DOS_PUNTOS` | `:` |
 
---

## 4. Demostración LL(1)

Una gramática es LL(1) si y solo si, para cada no-terminal `A` con producciones `A → α₁ | α₂ | ... | αₙ`, se cumplen **todas** las condiciones siguientes para cada par `i ≠ j`:

1. `FIRST(αᵢ) ∩ FIRST(αⱼ) = ∅`
2. Si `αᵢ ⇒* ε`, entonces `FIRST(αⱼ) ∩ FOLLOW(A) = ∅`
3. A lo sumo una `αᵢ` puede derivar `ε`
### 4.1 Conjuntos FIRST

Se define `FIRST(α)` como el conjunto de terminales que pueden aparecer como primer símbolo en alguna derivación de `α`. Si `α ⇒* ε`, entonces `ε ∈ FIRST(α)`.

#### No-terminales de programa y función

| No-terminal | FIRST |
|---|---|
| `PROGRAMA` | `{ chunche }` |
| `PROGRAMA'` | `{ chunche, ε }` |
| `DECL_FUNC` | `{ chunche }` |

#### Tipos

| No-terminal | FIRST |
|---|---|
| `TIPO` | `{ colones, diay, labia, ni_papa, fila_india }` |

#### Parámetros

| No-terminal | FIRST |
|---|---|
| `PARAMS` | `{ colones, diay, labia, ni_papa, fila_india, ε }` |
| `PARAMS'` | `{ ,, ε }` |
| `PARAM` | `{ colones, diay, labia, ni_papa, fila_india }` |

#### Bloque y sentencias

Sea `FIRST_SENTENCIA` = `{ vara, de_fijo, tomela, mae, bretee, miau, jaleas, dele_dele, IDENT }`.

| No-terminal | FIRST |
|---|---|
| `BLOQUE` | `{ { }` |
| `SENTENCIAS` | `FIRST_SENTENCIA` |
| `SENTENCIAS'` | `FIRST_SENTENCIA ∪ { ε }` |
| `SENTENCIA` | `FIRST_SENTENCIA` |
| `DECL_VAR` | `{ vara }` |
| `DECL_CONST` | `{ de_fijo }` |
| `SENT_RETORNO` | `{ tomela }` |
| `SENT_COND` | `{ mae }` |
| `SINO_OPC` | `{ tons, ε }` |
| `SENT_CICLO` | `{ bretee }` |
| `SENT_PRINT` | `{ miau }` |
| `SENT_BREAK` | `{ jaleas }` |
| `SENT_CONTINUE` | `{ dele_dele }` |
| `SENT_EXPR` | `{ IDENT }` |
| `SENT_IDENT_COLA` | `{ =, (, ++, -- }` |
| `SENT_INDEX_COLA` | `{ = }` |

#### Argumentos

| No-terminal | FIRST |
|---|---|
| `ARGS` | `FIRST(EXPRESION) ∪ { ε }` |
| `ARGS'` | `{ ,, ε }` |

#### Expresiones

Sea `FIRST_FACTOR` = `{ !, -, (, [, IDENT, LIT_ENTERO, LIT_STRING, diay_si, diay_no, me_la_comi }`.

| No-terminal | FIRST |
|---|---|
| `EXPRESION` | `FIRST_FACTOR` |
| `OR_EXPR` | `FIRST_FACTOR` |
| `OR_EXPR'` | `{ \|\|, ε }` |
| `AND_EXPR` | `FIRST_FACTOR` |
| `AND_EXPR'` | `{ &&, ε }` |
| `EQUAL_EXPR` | `FIRST_FACTOR` |
| `EQUAL_EXPR'` | `{ ==, !=, ε }` |
| `RELAC_EXPR` | `FIRST_FACTOR` |
| `RELAC_EXPR'` | `{ <, <=, >, >=, ε }` |
| `ARIT_EXPR` | `FIRST_FACTOR` |
| `ARIT_EXPR'` | `{ +, -, ε }` |
| `TERMINO` | `FIRST_FACTOR` |
| `TERMINO'` | `{ *, /, ε }` |
| `FACTOR` | `FIRST_FACTOR` |
| `FACTOR_BASE` | `{ (, [, IDENT, LIT_ENTERO, LIT_STRING, diay_si, diay_no, me_la_comi }` |
| `FACTOR'` | `{ ++, --, (, ε }` |
| `FACTOR_INDEX` | `{ (, ε }` |
 
---

### 4.2 Conjuntos FOLLOW

Se define `FOLLOW(A)` como el conjunto de terminales que pueden aparecer inmediatamente a la derecha de `A` en alguna forma sentencial. `EOF` se añade a `FOLLOW` del símbolo inicial.

| No-terminal | FOLLOW |
|---|---|
| `PROGRAMA` | `{ EOF }` |
| `PROGRAMA'` | `{ EOF }` |
| `DECL_FUNC` | `{ chunche, EOF }` |
| `TIPO` | `{ IDENT, (, ) }` |
| `PARAMS` | `{ ) }` |
| `PARAMS'` | `{ ) }` |
| `PARAM` | `{ ,, ) }` |
| `BLOQUE` | `{ chunche, EOF, tons, vara, de_fijo, tomela, mae, bretee, miau, jaleas, dele_dele, IDENT, } }` |
| `SENTENCIAS` | `{ } }` |
| `SENTENCIAS'` | `{ } }` |
| `SENTENCIA` | `FIRST_SENTENCIA ∪ { } }` |
| `DECL_VAR` | `FOLLOW(SENTENCIA)` |
| `DECL_CONST` | `FOLLOW(SENTENCIA)` |
| `SENT_RETORNO` | `FOLLOW(SENTENCIA)` |
| `SENT_COND` | `FOLLOW(SENTENCIA)` |
| `SINO_OPC` | `FOLLOW(SENTENCIA)` |
| `SENT_CICLO` | `FOLLOW(SENTENCIA)` |
| `SENT_PRINT` | `FOLLOW(SENTENCIA)` |
| `SENT_BREAK` | `FOLLOW(SENTENCIA)` |
| `SENT_CONTINUE` | `FOLLOW(SENTENCIA)` |
| `SENT_EXPR` | `FOLLOW(SENTENCIA)` |
| `SENT_IDENT_COLA` | `FOLLOW(SENTENCIA)` |
| `SENT_INDEX_COLA` | `FOLLOW(SENTENCIA)` |
| `ARGS` | `{ ) }` |
| `ARGS'` | `{ ) }` |
| `EXPRESION` | `{ ;, ), ,, } }` |
| `OR_EXPR` | `FOLLOW(EXPRESION)` |
| `OR_EXPR'` | `FOLLOW(EXPRESION)` |
| `AND_EXPR` | `{ \|\| } ∪ FOLLOW(OR_EXPR')` |
| `AND_EXPR'` | `FOLLOW(AND_EXPR)` |
| `EQUAL_EXPR` | `{ && } ∪ FOLLOW(AND_EXPR')` |
| `EQUAL_EXPR'` | `FOLLOW(EQUAL_EXPR)` |
| `RELAC_EXPR` | `{ ==, != } ∪ FOLLOW(EQUAL_EXPR')` |
| `RELAC_EXPR'` | `FOLLOW(RELAC_EXPR)` |
| `ARIT_EXPR` | `{ <, <=, >, >= } ∪ FOLLOW(RELAC_EXPR')` |
| `ARIT_EXPR'` | `FOLLOW(ARIT_EXPR)` |
| `TERMINO` | `{ +, - } ∪ FOLLOW(ARIT_EXPR')` |
| `TERMINO'` | `FOLLOW(TERMINO)` |
| `FACTOR` | `{ *, / } ∪ FOLLOW(TERMINO')` |
| `FACTOR_BASE` | `{ ++, --, (, ;, ), ,, }, *, /, +, -, <, <=, >, >=, ==, !=, &&, \|\| }` |
| `FACTOR'` | `FOLLOW(FACTOR)` |
| `FACTOR_INDEX` | `FOLLOW(FACTOR')` |
 
---

### 4.3 Tabla de Parsing LL(1)

Las producciones se numeran a continuación para referencia en la tabla:

```
1.  PROGRAMA         → DECL_FUNC PROGRAMA'
2.  PROGRAMA'        → DECL_FUNC PROGRAMA'
3.  PROGRAMA'        → ε
4.  DECL_FUNC        → chunche TIPO IDENT ( PARAMS ) BLOQUE : TIPO
5.  TIPO             → colones
6.  TIPO             → diay
7.  TIPO             → labia
8.  TIPO             → ni_papa
9.  TIPO             → fila_india ( TIPO )
10. PARAMS           → PARAM PARAMS'
11. PARAMS           → ε
12. PARAMS'          → , PARAM PARAMS'
13. PARAMS'          → ε
14. PARAM            → TIPO IDENT
15. BLOQUE           → { SENTENCIAS }
16. SENTENCIAS       → SENTENCIA SENTENCIAS'
17. SENTENCIAS'      → SENTENCIA SENTENCIAS'
18. SENTENCIAS'      → ε
19. SENTENCIA        → DECL_VAR
20. SENTENCIA        → DECL_CONST
21. SENTENCIA        → SENT_RETORNO
22. SENTENCIA        → SENT_COND
23. SENTENCIA        → SENT_CICLO
24. SENTENCIA        → SENT_PRINT
25. SENTENCIA        → SENT_BREAK
26. SENTENCIA        → SENT_CONTINUE
27. SENTENCIA        → SENT_EXPR
28. DECL_VAR         → vara TIPO IDENT = EXPRESION ;
29. DECL_CONST       → de_fijo TIPO IDENT = EXPRESION ;
30. SENT_RETORNO     → tomela EXPRESION ;
31. SENT_COND        → mae ( EXPRESION ) BLOQUE SINO_OPC
32. SINO_OPC         → tons BLOQUE
33. SINO_OPC         → ε
34. SENT_CICLO       → bretee ( EXPRESION ) BLOQUE
35. SENT_BREAK       → jaleas ;
36. SENT_CONTINUE    → dele_dele ;
37. SENT_PRINT       → miau ( ARGS ) ;
38. SENT_EXPR        → IDENT SENT_IDENT_COLA
39. SENT_IDENT_COLA  → = EXPRESION ;
40. SENT_IDENT_COLA  → ( ARGS ) ;
41. SENT_IDENT_COLA  → [ EXPRESION ] SENT_INDEX_COLA
42. SENT_IDENT_COLA  → ++ ;
43. SENT_IDENT_COLA  → -- ;
44. SENT_INDEX_COLA  → = EXPRESION ;
45. SENT_INDEX_COLA  → [ EXPRESION ] SENT_INDEX_COLA
46. ARGS             → EXPRESION ARGS'
47. ARGS             → ε
48. ARGS'            → , EXPRESION ARGS'
49. ARGS'            → ε
50. EXPRESION        → OR_EXPR
51. OR_EXPR          → AND_EXPR OR_EXPR'
52. OR_EXPR'         → || AND_EXPR OR_EXPR'
53. OR_EXPR'         → ε
54. AND_EXPR         → EQUAL_EXPR AND_EXPR'
55. AND_EXPR'        → && EQUAL_EXPR AND_EXPR'
56. AND_EXPR'        → ε
57. EQUAL_EXPR       → RELAC_EXPR EQUAL_EXPR'
58. EQUAL_EXPR'      → == RELAC_EXPR EQUAL_EXPR'
59. EQUAL_EXPR'      → != RELAC_EXPR EQUAL_EXPR'
60. EQUAL_EXPR'      → ε
61. RELAC_EXPR       → ARIT_EXPR RELAC_EXPR'
62. RELAC_EXPR'      → < ARIT_EXPR RELAC_EXPR'
63. RELAC_EXPR'      → <= ARIT_EXPR RELAC_EXPR'
64. RELAC_EXPR'      → > ARIT_EXPR RELAC_EXPR'
65. RELAC_EXPR'      → >= ARIT_EXPR RELAC_EXPR'
66. RELAC_EXPR'      → ε
67. ARIT_EXPR        → TERMINO ARIT_EXPR'
68. ARIT_EXPR'       → + TERMINO ARIT_EXPR'
69. ARIT_EXPR'       → - TERMINO ARIT_EXPR'
70. ARIT_EXPR'       → ε
71. TERMINO          → FACTOR TERMINO'
72. TERMINO'         → * FACTOR TERMINO'
73. TERMINO'         → / FACTOR TERMINO'
74. TERMINO'         → ε
75. FACTOR           → ! FACTOR
76. FACTOR           → - FACTOR
77. FACTOR           → FACTOR_BASE FACTOR'
78. FACTOR_BASE      → ( EXPRESION )
79. FACTOR_BASE      → [ LISTA_ELEMENTOS ]
80. FACTOR_BASE      → IDENT
81. FACTOR_BASE      → LIT_ENTERO
82. FACTOR_BASE      → LIT_STRING
83. FACTOR_BASE      → diay_si
84. FACTOR_BASE      → diay_no
85. FACTOR_BASE      → me_la_comi ( )
86. LISTA_ELEMENTOS  → EXPRESION LISTA_ELEMENTOS'
87. LISTA_ELEMENTOS  → ε
88. LISTA_ELEMENTOS' → , EXPRESION LISTA_ELEMENTOS'
89. LISTA_ELEMENTOS' → ε
90. FACTOR'          → ++
91. FACTOR'          → --
92. FACTOR'          → ( ARGS )
93. FACTOR'          → [ EXPRESION ] FACTOR_INDEX
94. FACTOR'          → ε
95. FACTOR_INDEX     → [ EXPRESION ] FACTOR_INDEX
96. FACTOR_INDEX     → ε
```

> Sea `FF` = `{ !, -, (, [, IDENT, LIT_ENTERO, LIT_STRING, diay_si, diay_no, me_la_comi }` y `FS` = `{ vara, de_fijo, tomela, mae, bretee, miau, jaleas, dele_dele, IDENT }` para abreviar.

| No-terminal | `chunche` | `vara` | `de_fijo` | `colones` | `diay` | `labia` | `ni_papa` | `fila_india` | `mae` | `tons` | `bretee` | `tomela` | `miau` | `me_la_comi` | `jaleas` | `dele_dele` | `diay_si` | `diay_no` | `IDENT` | `LIT_ENTERO` | `LIT_STRING` | `(` | `)` | `[` | `]` | `{` | `}` | `;` | `,` | `=` | `==` | `!=` | `<` | `<=` | `>` | `>=` | `&&` | `\|\|` | `!` | `+` | `-` | `*` | `/` | `++` | `--` | `EOF` |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `PROGRAMA` | 1 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `PROGRAMA'` | 2 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 3 |
| `DECL_FUNC` | 4 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `TIPO` | — | — | — | 5 | 6 | 7 | 8 | 9 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `PARAMS` | — | — | — | 10 | 10 | 10 | 10 | 10 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 11 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `PARAMS'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 13 | — | — | — | — | — | 12 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `PARAM` | — | — | — | 14 | 14 | 14 | 14 | 14 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `BLOQUE` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 15 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENTENCIAS` | — | 16 | 16 | — | — | — | — | — | 16 | — | 16 | 16 | 16 | — | 16 | 16 | — | — | 16 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENTENCIAS'` | — | 17 | 17 | — | — | — | — | — | 17 | — | 17 | 17 | 17 | — | 17 | 17 | — | — | 17 | — | — | — | — | — | — | — | 18 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENTENCIA` | — | 19 | 20 | — | — | — | — | — | 22 | — | 23 | 21 | 24 | — | 25 | 26 | — | — | 27 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `DECL_VAR` | — | 28 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `DECL_CONST` | — | — | 29 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_RETORNO` | — | — | — | — | — | — | — | — | — | — | — | 30 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_COND` | — | — | — | — | — | — | — | — | 31 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SINO_OPC` | — | 33 | 33 | — | — | — | — | — | 33 | 32 | 33 | 33 | 33 | — | 33 | 33 | — | — | 33 | — | — | — | — | — | — | — | 33 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 33 |
| `SENT_CICLO` | — | — | — | — | — | — | — | — | — | — | 34 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_BREAK` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 35 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_CONTINUE` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 36 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_PRINT` | — | — | — | — | — | — | — | — | — | — | — | — | 37 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 38 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `SENT_IDENT_COLA` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 40 | — | 41 | — | — | — | — | — | 39 | — | — | — | — | — | — | — | — | — | — | — | — | — | 42 | 43 | — |
| `SENT_INDEX_COLA` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 45 | — | — | — | — | — | 44 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `ARGS` | — | — | — | — | — | — | — | — | — | — | — | — | — | 46 | — | — | 46 | 46 | 46 | 46 | 46 | 46 | 47 | 46 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 46 | — | 46 | — | — | — | — | — |
| `ARGS'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 49 | — | — | — | — | — | 48 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `EXPRESION` | — | — | — | — | — | — | — | — | — | — | — | — | — | 50 | — | — | 50 | 50 | 50 | 50 | 50 | 50 | — | 50 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 50 | — | 50 | — | — | — | — | — |
| `OR_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 51 | — | — | 51 | 51 | 51 | 51 | 51 | 51 | — | 51 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 51 | — | 51 | — | — | — | — | — |
| `OR_EXPR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 53 | — | 53 | — | 53 | 53 | 53 | — | — | — | — | — | — | — | — | 52 | — | — | — | — | — | — | — | — |
| `AND_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 54 | — | — | 54 | 54 | 54 | 54 | 54 | 54 | — | 54 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 54 | — | 54 | — | — | — | — | — |
| `AND_EXPR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 56 | — | 56 | — | 56 | 56 | 56 | — | — | — | — | — | — | — | 55 | 56 | — | — | — | — | — | — | — | — |
| `EQUAL_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 57 | — | — | 57 | 57 | 57 | 57 | 57 | 57 | — | 57 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 57 | — | 57 | — | — | — | — | — |
| `EQUAL_EXPR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 60 | — | 60 | — | 60 | 60 | 60 | — | 58 | 59 | — | — | — | — | 60 | 60 | — | — | — | — | — | — | — | — |
| `RELAC_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 61 | — | — | 61 | 61 | 61 | 61 | 61 | 61 | — | 61 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 61 | — | 61 | — | — | — | — | — |
| `RELAC_EXPR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 66 | — | 66 | — | 66 | 66 | 66 | — | 66 | 66 | 62 | 63 | 64 | 65 | 66 | 66 | — | — | — | — | — | — | — | — |
| `ARIT_EXPR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 67 | — | — | 67 | 67 | 67 | 67 | 67 | 67 | — | 67 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 67 | — | 67 | — | — | — | — | — |
| `ARIT_EXPR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 70 | — | 70 | — | 70 | 70 | 70 | — | 70 | 70 | 70 | 70 | 70 | 70 | 70 | 70 | — | 68 | 69 | — | — | — | — | — |
| `TERMINO` | — | — | — | — | — | — | — | — | — | — | — | — | — | 71 | — | — | 71 | 71 | 71 | 71 | 71 | 71 | — | 71 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 71 | — | 71 | — | — | — | — | — |
| `TERMINO'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 74 | — | 74 | — | 74 | 74 | 74 | — | 74 | 74 | 74 | 74 | 74 | 74 | 74 | 74 | — | 74 | 74 | 72 | 73 | — | — | — |
| `FACTOR` | — | — | — | — | — | — | — | — | — | — | — | — | — | 77 | — | — | 77 | 77 | 77 | 77 | 77 | 77 | — | 77 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 75 | — | 76 | — | — | — | — | — |
| `FACTOR_BASE` | — | — | — | — | — | — | — | — | — | — | — | — | — | 85 | — | — | 83 | 84 | 80 | 81 | 82 | 78 | — | 79 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `LISTA_ELEMENTOS` | — | — | — | — | — | — | — | — | — | — | — | — | — | 86 | — | — | 86 | 86 | 86 | 86 | 86 | 86 | — | 86 | 87 | — | — | — | — | — | — | — | — | — | — | — | — | — | 86 | — | 86 | — | — | — | — | — |
| `LISTA_ELEMENTOS'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 89 | — | — | — | 88 | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — |
| `FACTOR'` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 92 | 94 | 93 | 94 | — | 94 | 94 | 94 | — | 94 | 94 | 94 | 94 | 94 | 94 | 94 | 94 | — | 94 | 94 | 94 | 94 | 90 | 91 | — |
| `FACTOR_INDEX` | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | — | 96 | — | 96 | — | 96 | 96 | 96 | — | 96 | 96 | 96 | 96 | 96 | 96 | 96 | 96 | — | 96 | 96 | 96 | 96 | — | — | — |

### 4.4 Conclusión

La gramática CR++ es **LL(1)** porque la tabla de parsing no tiene conflictos: ninguna celda contiene más de una producción. Esto significa que para cualquier combinación de no-terminal y token de lookahead, el parser de descenso recursivo siempre sabe exactamente qué producción aplicar, sin ambigüedades ni necesidad de retroceso.
