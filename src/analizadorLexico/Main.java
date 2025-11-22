package analizadorLexico;

import analizadorLexico.simbolos.TablaSimbolos;
import analizadorLexico.tokens.TipoToken;
import analizadorLexico.tokens.Token;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Proporcione la ruta al fichero fuente.");
            return;
        }

        String ficheroErrores = args[0] + ".errores.txt";
        String ficheroTokens = args[0] + ".tokens.txt";
        String ficheroSimbolos = args[0] + ".simbolos.txt";

        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        ALex aLex;

        try(BufferedWriter tokensWriter = new BufferedWriter(new FileWriter(ficheroTokens));
            BufferedWriter errorsWriter = new BufferedWriter(new FileWriter(ficheroErrores))){

            aLex = new ALex(tablaSimbolos, args[0]);

            Token token;
            boolean hayError = false;
            do {
                token = aLex.getNextToken();

                if(token.tipo == TipoToken.Tipo.ERROR){
                    hayError = true;
                    String errorMnsj = "Error en línea: " + token.linea + " | " + token.atributo + " (Lexema: '" + token.lexema + "')";

                    errorsWriter.write(errorMnsj);
                    errorsWriter.newLine();
                    System.err.println(errorMnsj);
                } else {
                    tokensWriter.write(token.toFileString());
                    tokensWriter.newLine();
                }

            } while (token.tipo != TipoToken.Tipo.EOF);

            System.out.println("Análisis Léxico finalizado.");
            System.out.println("Fichero de tokens -> '" + ficheroTokens + "'");
            if(hayError) System.out.println("Fichero de errores -> '" + ficheroErrores + "'");

        } catch (IOException e){
            System.err.println("Error al procesar los ficheros: " + e.getMessage());
            return;
        }

        try{
            tablaSimbolos.volcarAFichero(ficheroSimbolos);
            System.out.println("Tabla de simbolos -> '" + ficheroSimbolos + "'");
        } catch (IOException e) {
            System.err.println("Error al volcar la tabla de simbolos: " + e.getMessage());
        }

    }
}
