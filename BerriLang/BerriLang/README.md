# 🏴‍☠️ BerriLang — Mini Compilador One Piece

**Universidad Mariano Gálvez de Guatemala**  
Curso: Compiladores — Campus Jutiapa 2026  
Catedrática: Inga. M.A. Sheyla Esquivel

---

## Requisitos

- Java 17 o superior
- Maven 3.8+
- IntelliJ IDEA (recomendado)

## Cómo abrir en IntelliJ IDEA

1. Abre IntelliJ IDEA
2. File → Open → selecciona la carpeta `BerriLang`
3. IntelliJ detectará el `pom.xml` automáticamente
4. Espera que descargue las dependencias (Maven)
5. Ejecuta `MainWindow.java` (clic derecho → Run)

## Cómo compilar por terminal

```bash
mvn clean package
java -jar target/BerriLang-1.0-SNAPSHOT-shaded.jar
```

## Estructura del proyecto

```
BerriLang/
├── pom.xml
├── GRAMATICA_BNF.md
├── pruebas.berry
└── src/main/java/com/berrylang/
    ├── lexer/
    │   └── Lexer.java          ← Analizador léxico
    ├── parser/
    │   ├── Parser.java         ← Analizador sintáctico
    │   └── NodoAST.java        ← Nodos del árbol AST
    ├── model/
    │   ├── Token.java          ← Definición de tokens
    │   ├── ErrorEntry.java     ← Modelo de errores
    │   └── SimboloEntry.java   ← Tabla de símbolos
    ├── ui/
    │   ├── MainWindow.java     ← Ventana principal Swing
    │   ├── TreeRenderer.java   ← Vista del árbol AST
    │   ├── TextLineNumber.java ← Numeración de líneas
    │   └── OnePieceTheme.java  ← Paleta de colores
    └── util/
        ├── Compiler.java       ← Fachada del compilador
        └── CompilerResult.java ← Resultado de compilación
```

## Sintaxis BerriLang — Guía rápida

```
// Comentario de línea

nakama NombrePrograma {
    // Declaraciones de variables
    yoru  numeroEntero = 10;
    zoro  numeroDecimal = 3.14;
    gomu  cadena = "Hola nakama!";
    haki  booleano = mera;

    // Imprimir
    nami(cadena);

    // Condicional
    luffy (numeroEntero > 5) {
        nami("Mayor que 5");
    } sino {
        nami("Menor o igual a 5");
    }

    // Ciclo while
    chopper (numeroEntero > 0) {
        nami(numeroEntero);
        numeroEntero = numeroEntero - 1;
    }

    // Definir función
    usopp miFuncion(yoru x, yoru y) {
        yoru r = x + y;
        sanji r;
    }
}
kaizoku
```

## Palabras reservadas

| Keyword   | Significado         |
|-----------|---------------------|
| nakama    | inicio del programa |
| kaizoku   | fin del programa    |
| yoru      | tipo entero         |
| zoro      | tipo decimal        |
| gomu      | tipo cadena         |
| haki      | tipo booleano       |
| luffy     | if / si             |
| sino      | else / sino         |
| chopper   | while / mientras    |
| robin     | for / para          |
| nami      | print / imprimir    |
| usopp     | función             |
| sanji     | return / retornar   |
| denden    | input / leer        |
| mera      | true / verdadero    |
| kairyu    | false / falso       |
