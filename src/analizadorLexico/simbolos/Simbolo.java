package analizadorLexico.simbolos;

public class Simbolo {
    public final String lexema;
    public final int id;

    public Simbolo(String nombre, int id) {
        this.lexema = nombre;
        this.id = id;
    }

    @Override
    public String toString(){
        String str = "ID: " + this.id + " |   Lexema: " + this.lexema;
        return str;
    }
}
