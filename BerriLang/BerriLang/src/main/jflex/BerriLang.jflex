/* ============================================================
   BerriLang.jflex
   Especificación JFlex para el analizador léxico de BerriLang
   Universidad Mariano Gálvez - Compiladores 2026

   ESTRUCTURA DE UN ARCHIVO .jflex:
   ─────────────────────────────────
   SECCIÓN 1: Opciones y código Java inicial
   %%
   SECCIÓN 2: Macros / Definiciones de expresiones regulares
   %%
   SECCIÓN 3: Reglas léxicas (patrón → acción)
   ============================================================ */


/* ============================================================
   SECCIÓN 1 — Opciones y código Java
   Aquí va:
     - El paquete Java donde se generará la clase
     - Los imports necesarios
     - Directivas de JFlex (%class, %type, %line, %column)
     - Código Java que se copia literalmente al inicio
       de la clase generada (dentro de %{ ... %})
   ============================================================ */

package com.berrylang.lexer;

import com.berrylang.model.Token;
import com.berrylang.model.Token.TokenType;
import com.berrylang.model.ErrorEntry;
import com.berrylang.model.ErrorEntry.TipoError;
import java.util.ArrayList;
import java.util.List;

/* Directivas JFlex */
%class      BerriLexer          /* nombre de la clase Java que se generará  */
%type       Token               /* tipo de retorno del método yylex()        */
%unicode                        /* soporte Unicode completo                  */
%line                           /* activa el contador de líneas (yyline)     */
%column                         /* activa el contador de columnas (yycolumn) */
%public                         /* la clase generada será public             */

/* Código Java que se inyecta dentro de la clase generada */
%{
    /* Lista de errores léxicos encontrados */
    private List<ErrorEntry> errores = new ArrayList<>();

    public List<ErrorEntry> getErrores() { return errores; }

    /* Método auxiliar para registrar errores */
    private void registrarError(String descripcion, String causa) {
        errores.add(new ErrorEntry(
            TipoError.LEXICO,
            descripcion,
            yyline + 1,     /* JFlex empieza en 0, nosotros en 1 */
            yycolumn + 1,
            causa
        ));
    }

    /* Crea un token con la posición actual */
    private Token token(TokenType tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1);
    }
%}


/* ============================================================
   SECCIÓN 2 — Macros (definiciones de expresiones regulares)
   Aquí defines nombres cortos para patrones que usarás
   repetidamente en las reglas. La sintaxis es:
       NOMBRE = expresion_regular
   Luego en las reglas se usan como {NOMBRE}
   ============================================================ */

%%

/* Caracteres básicos */
LETRA        = [a-zA-Z_]
DIGITO       = [0-9]
ESPACIO      = [ \t\r\n\f]

/* Tokens de valor */
ENTERO       = {DIGITO}+
DECIMAL      = {DIGITO}+\.{DIGITO}+
ID           = {LETRA}({LETRA}|{DIGITO})*

/* Cadenas de texto:  "contenido con \" escapado" */
CADENA       = \"([^\"\\\n]|\\.)*\"

/* Comentarios */
COMENT_LINEA = "//"[^\n]*
COMENT_BLOQUE = "/*"([^*]|\*+[^*/])*\*+"/"


/* ============================================================
   SECCIÓN 3 — Reglas léxicas
   Cada regla tiene el formato:
       patron    { acción Java }
   JFlex prueba los patrones en orden. Si hay ambigüedad
   gana el patrón más largo (regla del máximo munch).
   Si dos patrones tienen la misma longitud gana el primero.

   IMPORTANTE: Las palabras reservadas deben ir ANTES del
   patrón general {ID} para tener prioridad.
   ============================================================ */

%%

/* ── Espacios y comentarios (se ignoran, no producen token) ── */
{ESPACIO}        { /* ignorar espacios, tabs y saltos de línea */ }
{COMENT_LINEA}   { /* ignorar comentarios de línea            */ }
{COMENT_BLOQUE}  { /* ignorar comentarios de bloque           */ }


/* ── Palabras reservadas ─────────────────────────────────────
   Deben ir ANTES de la regla {ID} para tener precedencia.
   JFlex ya aplica la regla del máximo munch, pero si la
   longitud es igual gana la que aparece primero.
   ─────────────────────────────────────────────────────────── */

"nakama"   { return token(TokenType.NAKAMA);   }  /* inicio del programa */
"kaizoku"  { return token(TokenType.KAIZOKU);  }  /* fin del programa    */
"yoru"     { return token(TokenType.YORU);     }  /* tipo entero         */
"zoro"     { return token(TokenType.ZORO);     }  /* tipo decimal        */
"gomu"     { return token(TokenType.GOMU);     }  /* tipo cadena         */
"haki"     { return token(TokenType.HAKI);     }  /* tipo booleano       */
"luffy"    { return token(TokenType.LUFFY);    }  /* if                  */
"sino"     { return token(TokenType.NAMI_ELSE);}  /* else                */
"chopper"  { return token(TokenType.CHOPPER);  }  /* while               */
"robin"    { return token(TokenType.ROBIN);    }  /* for                 */
"nami"     { return token(TokenType.NAMI);     }  /* print               */
"usopp"    { return token(TokenType.USOPP);    }  /* definir función     */
"sanji"    { return token(TokenType.SANJI);    }  /* return              */
"denden"   { return token(TokenType.DENDEN);   }  /* input               */
"mera"     { return token(TokenType.MERA);     }  /* true                */
"kairyu"   { return token(TokenType.KAIRYU);   }  /* false               */


/* ── Identificadores (después de las palabras reservadas) ── */
{ID}       { return token(TokenType.ID); }


/* ── Literales numéricos ────────────────────────────────────
   DECIMAL antes que ENTERO: "3.14" no debe tokenizarse como
   ENTERO("3") + PUNTO + ENTERO("14")
   ─────────────────────────────────────────────────────────── */
{DECIMAL}  { return token(TokenType.DECIMAL); }
{ENTERO}   { return token(TokenType.ENTERO);  }


/* ── Cadenas de texto ─────────────────────────────────────── */
{CADENA}   {
    /* yytext() incluye las comillas; las quitamos para el valor */
    String valor = yytext().substring(1, yytext().length() - 1);
    return new Token(TokenType.CADENA, valor, yyline + 1, yycolumn + 1);
}

/* Cadena sin cerrar (llega a fin de línea sin comilla de cierre) */
\"[^\"\n]*  {
    registrarError("Cadena de texto sin cerrar",
                   "Se encontró '\"' sin su cierre antes del fin de línea");
    return token(TokenType.ERROR);
}


/* ── Operadores de dos caracteres (van ANTES de los de 1) ── */
"=="  { return token(TokenType.IGUAL_IGUAL);   }
"!="  { return token(TokenType.DIFERENTE);     }
"<="  { return token(TokenType.MENOR_IGUAL);   }
">="  { return token(TokenType.MAYOR_IGUAL);   }
"&&"  { return token(TokenType.Y);             }
"||"  { return token(TokenType.O);             }
"->"  { return token(TokenType.FLECHA);        }


/* ── Operadores de un carácter ───────────────────────────── */
"+"   { return token(TokenType.MAS);           }
"-"   { return token(TokenType.MENOS);         }
"*"   { return token(TokenType.POR);           }
"/"   { return token(TokenType.DIVIDIR);       }
"%"   { return token(TokenType.MODULO);        }
"="   { return token(TokenType.ASIGNAR);       }
"<"   { return token(TokenType.MENOR);         }
">"   { return token(TokenType.MAYOR);         }
"!"   { return token(TokenType.NO);            }


/* ── Delimitadores ───────────────────────────────────────── */
"("   { return token(TokenType.PAREN_ABRE);    }
")"   { return token(TokenType.PAREN_CIERRA);  }
"{"   { return token(TokenType.LLAVE_ABRE);    }
"}"   { return token(TokenType.LLAVE_CIERRA);  }
";"   { return token(TokenType.PUNTO_COMA);    }
":"   { return token(TokenType.DOS_PUNTOS);    }
","   { return token(TokenType.COMA);          }


/* ── Error léxico: carácter no reconocido ───────────────── */
.  {
    registrarError(
        "Carácter no permitido: '" + yytext() + "'",
        "El carácter '" + yytext() + "' (ASCII " + (int)yytext().charAt(0) +
        ") no pertenece al alfabeto de BerriLang"
    );
    return token(TokenType.ERROR);
}


/* ── Fin de archivo ─────────────────────────────────────── */
<<EOF>>    { return token(TokenType.EOF); }
