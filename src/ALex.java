import simbolos.TablaSimbolos;
import tokens.TipoToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ALex {
    private final BufferedReader reader;
    private int linea;
    private int nextChar;

    private final TablaSimbolos tablaSimbolos;
    private static final Map<String, TipoToken.Tipo> palabrasReservadas;

    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("boolean", TipoToken.Tipo.BOOLEAN);
        palabrasReservadas.put("break", TipoToken.Tipo.BREAK);
        palabrasReservadas.put("case", TipoToken.Tipo.CASE);
        palabrasReservadas.put("default", TipoToken.Tipo.DEFAULT);
        palabrasReservadas.put("float", TipoToken.Tipo.FLOAT);
        palabrasReservadas.put("function", TipoToken.Tipo.FUNCTION);
        palabrasReservadas.put("if", TipoToken.Tipo.IF);
        palabrasReservadas.put("int", TipoToken.Tipo.INT);
        palabrasReservadas.put("let", TipoToken.Tipo.LET);
        palabrasReservadas.put("read", TipoToken.Tipo.READ);
        palabrasReservadas.put("return", TipoToken.Tipo.RETURN);
        palabrasReservadas.put("string", TipoToken.Tipo.STRING);
        palabrasReservadas.put("switch", TipoToken.Tipo.SWITCH);
        palabrasReservadas.put("void", TipoToken.Tipo.VOID);
        palabrasReservadas.put("write", TipoToken.Tipo.WRITE);
    }

    public ALex(TablaSimbolos tablaSimbolos, String filePath) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.tablaSimbolos = tablaSimbolos;
        this.linea = 1;
        this.nextChar = -1;
    }
}
