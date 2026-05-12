# Especificación semántica operativa

## 1. Tabla de símbolos

El analizador semántico mantiene una tabla de símbolos `st` organizada como una pila de ámbitos. Cada entrada registra los siguientes campos:

```
Simbolo {
    nombre   : String
    tipoDato : TipoDato
    categoria: "variable" | "constante" | "funcion" | "parametro"
    mutable  : Boolean
    // Para categoria = "funcion":
    retorno  : TipoDato
    params   : List<Parametro>
    cuerpo   : Bloque
    esEntrada: Boolean
}
```

Operaciones:

- `st.newScope()` — abre un nuevo ámbito.
- `st.pop()` — cierra el ámbito actual.
- `st[nombre] = simbolo` — registra un símbolo en el ámbito actual. Salta error si el nombre ya existe en la tabla.
- `st[nombre]` — busca el símbolo en el ámbito actual y los exteriores.
- `st.existe(nombre)` — verifica si un nombre está visible en algún ámbito.

---

## 2. Reglas por nodo

### Nodo: Programa(cuerpo)

Para cada `FuncionDecl f` en `cuerpo`:

```
st[f.nombre] = (f.nombre, f.retorno, f.params, f.cuerpo, f.esEntrada)
    // salta error si el nombre ya está en tabla
```

Luego verificar que en `st` exista exactamente una `FuncionDecl f` tal que `f.esEntrada = true`:

```
x si el nombre de esa función no es "chante"
x si chante.retorno es diferente a TipoNiPapa
x si chante.params no está vacío
```

---

### Nodo: FuncionDecl(nombre, retorno, params, cuerpo, esEntrada)

```
symbolT_$ con padre symbolT

para cada Parametro p en params:
    symbolT_$[p.nombre] = (p.nombre, p.tipoDato, categoria = parametro)
    // salta error si el nombre ya está en tabla

analizar cuerpo
salir de symbolT_$
```

---

### Nodo: VariableDecl(nombre, tipoDato, valor)

```
symbolT actual lanza error con nombre ya existente

symbolT[nombre] = (nombre, tipoDato, categoria = variable, mutable = true)
```

---

### Nodo: ConstanteDecl(nombre, tipoDato, valor)

```
salta error si nombre ya existe

symbolT[nombre] = (nombre, tipoDato, categoria = constante, mutable = false)
```

---

### Nodo: Identificador(nombre)

```
salta error si no existe el nombre

Identificador.tipo    = st[nombre].tipo
Identificador.mutable = st[nombre].mutable
```

---

### Nodo: Bloque(sentencias)

```
st.newScope
analizar sentencias
st.pop
```

---

### Nodo: AsignacionSimple(nombre, valor)

Salta error si:

```
x nombre no existe en st
x hay reasignación y mutable es falso
x st[nombre].tipo diferente de valor.tipo
```

---

### Nodo: AsignacionArray(nombre, indice, valor)

Salta error si:

```
x st[nombre] no existe
x st[nombre].mutable es falso y es reasignación
x st[nombre].tipo no es arreglo
x indice.tipo no es TipoColones (entero)
x indice.valor es menor a 0 y mayor o igual a la longitud del array
x st[nombre].tipo.tipoArray diferente a valor.tipo
```

---

### Nodo: Retorno(valor)

```
si valor.tipo diferente al valor de retorno permitido en el
scope de la función, salta error
```

---

### Nodo: Condicional(condicion, entonces_cuerpo, otro_cuerpo)

```
salta error si condicion.tipo diferente a TipoDiay

st.newScope
    analiza entonces_cuerpo
st.pop
st.newScope
    analiza otro_cuerpo
st.pop
```

---

### Nodo: Ciclo(condicion, cuerpo)

```
salta error si condicion.tipo diferente a TipoDiay

st.newScope
    analiza cuerpo
st.pop
```

---

### Nodo: Ruptura

```
si no estamos dentro de un Ciclo:
    error "jaleas solo puede usarse dentro de un bretee"
```

---

### Nodo: Continuar

```
si no estamos dentro de un Ciclo:
    error "dele_dele solo puede usarse dentro de un bretee"
```

---

### Nodo: Imprimir(args)

```
para cada Expresion e en args:
    si e.tipo = TipoNiPapa:
        error "no se puede imprimir un valor de tipo ni_papa"
```

---

### Nodo: LlamadaFuncion(nombre, args)

```
si not st.existe(nombre):
    error "función no declarada: nombre"

si st[nombre].categoria != funcion:
    error "nombre no es una función"

sea firma = st[nombre]

si len(args) != len(firma.params):
    error "cantidad de argumentos incorrecta para nombre"

para cada (arg, param) en zip(args, firma.params):
    si arg.tipo != param.tipo:
        error "tipo de argumento incompatible en llamada a nombre"

LlamadaFuncion.tipo = firma.retorno
```

---

### Nodo: AccesoArray(nombre, indice)

```
si not st.existe(nombre):
    error "variable no declarada: nombre"

si st[nombre].tipo no es de TipoArray:
    error "nombre no es un arreglo"

si indice.tipo != TipoColones:
    error "el índice debe ser de tipo colones"

AccesoArray.tipo = st[nombre].tipo.tipoElemento
```

---

### Nodo: InicializarArray(elementos)

```
si elementos está vacío:
    error "un arreglo debe tener al menos un elemento"

sea tipoBase = elementos[0].tipo

para cada Expresion e en elementos:
    si e.tipo != tipoBase:
        error "todos los elementos del arreglo deben ser del mismo tipo"

InicializarArray.tipo = TipoArray(tipoBase)
```

---

### Nodo: OperacionSuma(izq, der) / OperacionResta / OperacionMul / OperacionDiv

```
salta error si izq o der diferente a TipoColones

Operacion.tipo = TipoColones
```

---

### Nodo: OperacionIgu(izq, der) / OperacionDif(izq, der)

```
salta error si izq.tipo != der.tipo

Operacion.tipo = TipoDiay
```

---

### Nodo: OperacionMay(izq, der) / OperacionMen / OperacionMayIgu / OperacionMenIgu

```
si izq.tipo != TipoColones:
    error "operando izquierdo debe ser de tipo colones"

si der.tipo != TipoColones:
    error "operando derecho debe ser de tipo colones"

Operacion.tipo = TipoDiay
```

---

### Nodo: OperacionY(izq, der) / OperacionO

```
si izq.tipo != TipoDiay:
    error "operando izquierdo de operación lógica debe ser de tipo diay"

si der.tipo != TipoDiay:
    error "operando derecho de operación lógica debe ser de tipo diay"

Operacion.tipo = TipoDiay
```

---

### Nodo: OperacionNum(objetivo) — negación unaria `-`

```
si objetivo.tipo != TipoColones:
    error "el operador - unario solo aplica a tipo colones"

OperacionNum.tipo = TipoColones
```

---

### Nodo: OperacionDec(objetivo) — negación lógica `!`

```
si objetivo.tipo != TipoDiay:
    error "el operador ! solo aplica a tipo diay"

OperacionDec.tipo = TipoDiay
```

---

### Nodo: LiteralEntero(valor)

```
LiteralEntero.tipo = TipoColones
```

---

### Nodo: LiteralCadena(valor)

```
LiteralCadena.tipo = TipoLabia
```

---

### Nodo: LiteralBooleano(valor)

```
LiteralBooleano.tipo = TipoDiay
```

---

### Nodo: LiteralNulo

```
LiteralNulo.tipo = TipoNiPapa
```
