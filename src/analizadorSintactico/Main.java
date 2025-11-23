package analizadorSintactico; // OJO: Ahora el Main suele estar en el paquete padre o sintactico

import analizadorLexico.ALex;
import analizadorLexico.simbolos.TablaSimbolos;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Proporcione la ruta al fichero fuente.");
            return;
        }

        // 1. Definición de ficheros de salida
        String ficheroErrores = args[0] + ".errores.txt";
        String ficheroTokens = args[0] + ".tokens.txt";
        String ficheroSimbolos = args[0] + ".simbolos.txt";
        String ficheroParse = args[0] + ".parse.txt"; // ¡NUEVO!

        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        ALex aLex;
        ASint aSint;

        // 2. Abrimos TODOS los flujos de escritura
        try (BufferedWriter tokensWriter = new BufferedWriter(new FileWriter(ficheroTokens));
             BufferedWriter errorsWriter = new BufferedWriter(new FileWriter(ficheroErrores));
             BufferedWriter parseWriter = new BufferedWriter(new FileWriter(ficheroParse))) {

            // 3. Inicialización de componentes
            aLex = new ALex(tablaSimbolos, args[0]);
            Stack<Object> pila = new Stack<>();

            // 4. Inicializamos el ASint pasándole TODOS los writers que necesita
            // (Necesita tokensWriter y errorsWriter porque ahora EL es quien pide los tokens)
            aSint = new ASint(aLex, pila, parseWriter, tokensWriter, errorsWriter);

            System.out.println("Iniciando Análisis Sintáctico...");

            // 5. Cedemos el control al Analizador Sintáctico
            aSint.analizar();

            System.out.println("Análisis finalizado.");
            System.out.println("Generados: tokens, parse y errores (si los hay).");

        } catch (IOException e) {
            System.err.println("Error crítico de E/S: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 6. Volcado final de la Tabla de Símbolos (siempre al final)
        try {
            tablaSimbolos.volcarAFichero(ficheroSimbolos);
            System.out.println("Tabla de símbolos volcada en -> '" + ficheroSimbolos + "'");
        } catch (IOException e) {
            System.err.println("Error al volcar la tabla de símbolos: " + e.getMessage());
        }
    }
}