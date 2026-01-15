package analizadorLexico.simbolos;

import java.util.ArrayList;
import java.util.List;

public class Simbolo {
    private static int contadorIds = 0;

    public final String lexema;
    public final int id;

    public String tipo;             // "int", "boolean", "void", "function", "error"
    public String categoria;        // "variable", "parametro", "funcion"
    public int despl;               // desplazamiento


    // Solo para funciones:
    public int numParam;
    public List<String> tiposParam; // Lista de tipos de los argumentos
    public String tipoRetorno;


    public Simbolo(String nombre) {
        this.lexema = nombre;
        this.tiposParam = new ArrayList<>();
        this.id = contadorIds++;
    }

    @Override
    public String toString() {
        return lexema;
    }

    public static int getAncho(String tipo) {
        switch (tipo) {
            case "int": return 1;
            case "float": return 2;
            case "boolean": return 1;
            case "string": return 64;
            default: return 0;
        }
    }
}
