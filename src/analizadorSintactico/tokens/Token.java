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

    @Override
    public String toString() {
        String atrib = (atributo != null) ? atributo.toString() : "ninguno";
        return "<" + this.tipo.name() + "," + this.lexema + "," + atrib + "> (Linea: " + this.linea + ")";
    }

    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(this.tipo.codigo);

        if (this.atributo != null) {
            sb.append(",");
            if (this.tipo == TipoToken.Tipo.CADENA) {
                sb.append("'").append(this.atributo.toString()).append("'");
            } else {
                sb.append(this.atributo.toString());
            }
        } else {
            sb.append(",");
            sb.append("");
        }

        sb.append(">");
        return sb.toString();
    }
}
