package analizadorLexico.simbolos;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class GestorTablas {
    private Stack<TablaSimbolos> pilaAmbitos;
    private TablaSimbolos tablaGlobal;
    private int contadorTablas;

    public GestorTablas() {
        pilaAmbitos = new Stack<>();
        contadorTablas = 0;

        tablaGlobal = new TablaSimbolos(++contadorTablas);
        pilaAmbitos.push(tablaGlobal);
    }

    public void abrirAmbito() {
        TablaSimbolos nuevaTabla = new TablaSimbolos(++contadorTablas);
        pilaAmbitos.push(nuevaTabla);
    }

    public void cerrarAmbito(BufferedWriter writer) throws IOException {
        TablaSimbolos t = pilaAmbitos.pop();
        t.volcarAFichero(writer);
    }

    public Simbolo buscar(String lexema) {
        Simbolo s = pilaAmbitos.peek().buscar(lexema);
        if (s != null) return s;

        if (pilaAmbitos.peek() != tablaGlobal) {
            return tablaGlobal.buscar(lexema);
        }
        return null;
    }

    public Simbolo insertar(String lexema) {
        return pilaAmbitos.peek().insertar(lexema);
    }

    public TablaSimbolos getTablaActual() {
        return pilaAmbitos.peek();
    }
}