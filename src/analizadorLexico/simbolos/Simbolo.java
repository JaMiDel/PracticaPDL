package analizadorLexico.simbolos;

import java.util.ArrayList;
import java.util.List;

public class Simbolo {
    public final String lexema;
    public final int id;

    // --- ATRIBUTOS SEMÁNTICOS (Vitales para el paso 5) ---
    public String tipo;             // "int", "boolean", "void", "function", "error"
    public String categoria;        // "variable", "parametro", "funcion"
    public int despl;               // Dirección de memoria relativa (desplazamiento)


    // Solo para funciones:
    public int numParam;
    public List<String> tiposParam; // Lista de tipos de los argumentos: ["int", "string"]
    public String tipoRetorno;      // Qué devuelve la función
    public String etiqFuncion;      // Etiqueta en ensamblador (ej: "Func1")


    public Simbolo(String nombre) {
        this.lexema = nombre;
        this.tiposParam = new ArrayList<>();
        this.id = -1; // Se asignará al insertar en la tabla
    }

    // Método para el formato estricto de la TS (VASt compliant)
    @Override
    public String toString() {
        // TODO
        return lexema;
    }
}
