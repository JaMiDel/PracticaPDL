package analizadorSintactico.simbolos;

public class Simbolo {
    public final String lexema;
    public final int id;

    public Simbolo(String nombre, int id) {
        this.lexema = nombre;
        this.id = id;
    }
}
