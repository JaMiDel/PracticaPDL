package analizadorSintactico.tokens;

public class Token {
    public final TipoToken.Tipo tipo;
    public final String lexema;
    public final Object atributo;

    public final int linea;

    public Token(TipoToken.Tipo tipo, String lexema, Object atributo, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.atributo = atributo;
        this.linea = linea;
    }

    public String toFileString(){
        String atrib = (atributo != null) ? atributo.toString() : "ninguno";
        String str = "<" + this.tipo.name() + ", " + this.lexema + ", " + atrib + ">    Linea: " + this.linea;

        return str;
    }
}
