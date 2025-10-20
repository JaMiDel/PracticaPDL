package tokens;

public class Token {
    public final TipoToken tipo;
    public final String lexema;
    public final Object atributo;

    public final int linea;

    public Token(TipoToken tipo, String lexema, Object atributo, int linea) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.atributo = atributo;
        this.linea = linea;
    }

}
