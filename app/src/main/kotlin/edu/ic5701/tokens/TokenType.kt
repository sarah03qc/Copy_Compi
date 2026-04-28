package edu.ic5701.tokens

enum class TokenType {
    // Keywords
    FUNCION,        // chunche
    VARIABLE,       // vara
    CONSTANTE,      // de_fijo
    ENTERO,         // colones
    BOOLEANO,       // diay
    CADENA,         // labia
    VACIO,          // ni_papa
    ARREGLO,        // fila_india
    CONDICIONAL,    // mae
    SINO,           // tons
    CICLO_INDEF,    // bretee
    RETORNAR,       // tomela / tómela
    IMPRIMIR,       // miau
    RECIBIR,        // me_la_comi / me_la_comí
    BREAK,          // jaleas
    CONTINUE,       // dele_dele
    VERDADERO,      // diay_si / diay_sí
    FALSO,          // diay_no

    // Identificadores y literales
    IDENT,
    LIT_ENTERO,
    LIT_STRING,

    // Operadores aritméticos
    OP_SUMA,        // +
    OP_RESTA,       // -
    OP_MUL,         // *
    OP_DIV,         // /
    OP_INC,         // ++
    OP_DEC,         // --

    // Operadores de asignación y relacionales
    OP_ASIG,        // =
    OP_IGU,         // ==
    OP_DIFE,        // !=
    OP_MAYOR,       // >
    OP_MENOR,       // <
    OP_MAYOR_IG,    // >=
    OP_MENOR_IG,    // <=

    // Operadores lógicos
    OP_Y,           // &&
    OP_O,           // ||
    OP_NO,          // !

    // Delimitadores
    PAREN_ABIERTO,      // (
    PAREN_CERRADO,      // )
    LLAVE_ABIERTA,      // {
    LLAVE_CERRADA,      // }
    CORCHETE_ABIERTO,   // [
    CORCHETE_CERRADO,   // ]
    FIN_SENTENCIA,      // ;
    SEPARAR,            // ,
    DOS_PUNTOS,         // :

    // Especiales
    EOF,
    ERROR               // Jaguarsh
}