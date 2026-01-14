package analizadorLexico.simbolos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class GestorTablas {
    private Stack<TablaSimbolos> pilaAmbitos; // Pila LIFO de tablas
    private TablaSimbolos tablaGlobal;
    private int contadorTablas; // Para el ID #1, #2... del formato de salida

    public GestorTablas() {
        pilaAmbitos = new Stack<>();
        contadorTablas = 0;

        // Inicializamos con la tabla global
        tablaGlobal = new TablaSimbolos(++contadorTablas);
        pilaAmbitos.push(tablaGlobal);
    }

    // Entramos en una función -> Nueva tabla
    public void abrirAmbito() {
        TablaSimbolos nuevaTabla = new TablaSimbolos(++contadorTablas);
        pilaAmbitos.push(nuevaTabla);
    }

    // Salimos de una función -> Destruimos tabla (y la volcamos antes)
    public void cerrarAmbito(BufferedWriter writer) throws IOException {
        TablaSimbolos t = pilaAmbitos.pop();
        t.volcarAFichero(writer); // ¡Regla de Oro 2! Volcar antes de destruir
    }

    // Buscar símbolo: Primero en local, si no está, mirar en global
    public Simbolo buscar(String lexema) {
        // 1. Mirar en el ámbito actual (cima de la pila)
        Simbolo s = pilaAmbitos.peek().buscar(lexema);
        if (s != null) return s;

        // 2. Si no es el global, mirar en el global (MyJS solo permite 2 niveles de anidamiento)
        if (pilaAmbitos.peek() != tablaGlobal) {
            return tablaGlobal.buscar(lexema);
        }
        return null;
    }

    // Insertar: Siempre en el ámbito actual
    public Simbolo insertar(String lexema) {
        return pilaAmbitos.peek().insertar(lexema);
    }

    // Método para obtener la tabla actual (útil para añadir atributos)
    public TablaSimbolos getTablaActual() {
        return pilaAmbitos.peek();
    }
}