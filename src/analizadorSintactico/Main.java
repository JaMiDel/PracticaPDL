package analizadorSintactico;

import analizadorLexico.ALex;
import analizadorLexico.simbolos.GestorTablas;
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

        String ficheroFuente = args[0];
        String ficheroErrores = ficheroFuente + ".errores.txt";
        String ficheroTokens = ficheroFuente + ".tokens.txt";
        String ficheroSimbolos = ficheroFuente + ".simbolos.txt";
        String ficheroParse = ficheroFuente + ".parse.txt"; // ¡Nuevo fichero!

        GestorTablas gestor = new GestorTablas();

        try (
                BufferedWriter tokensWriter = new BufferedWriter(new FileWriter(ficheroTokens));
                BufferedWriter errorsWriter = new BufferedWriter(new FileWriter(ficheroErrores));
                BufferedWriter parseWriter = new BufferedWriter(new FileWriter(ficheroParse));

                BufferedWriter simbolosWriter = new BufferedWriter(new FileWriter(ficheroSimbolos))
        ) {
            // 1. Crear Léxico
            ALex aLex = new ALex(gestor, ficheroFuente);

            // 2. Crear Pila
            Stack<Object> pila = new Stack<>();

            // 3. Crear Sintáctico (Conectamos todo)
            ASint aSint = new ASint(aLex, pila, parseWriter, tokensWriter, errorsWriter);

            // 4. ¡EJECUTAR EL ANÁLISIS!
            System.out.println("Iniciando Análisis Sintáctico...");
            aSint.analizar();
            System.out.println("Análisis finalizado.");

            // 5. Volcar la Tabla Global (la última que queda en la pila)
            try {
                // Al cerrar el ámbito global, el gestor lo vuelca al fichero automáticamente
                gestor.cerrarAmbito(simbolosWriter);
                System.out.println("Tabla de símbolos generada.");
            } catch (IOException e) {
                System.err.println("Error volcando tabla de símbolos: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error grave de E/S: " + e.getMessage());
            e.printStackTrace();
        }
    }
}