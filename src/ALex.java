import simbolos.Simbolo;
import simbolos.TablaSimbolos;
import tokens.TipoToken;
import tokens.Token;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ALex {
    private final BufferedReader reader;
    private final int linea;
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

    public Token getNextToken() throws IOException {
        int estado = 0;
        StringBuilder lexema = new StringBuilder();
        int c;

        while(true){
            c = readChar();

            switch(estado){
                case 0:
                    if(Character.isLetter(c)){
                        lexema.append((char) c);
                        estado = 1;
                    }
                    if(Character.isDigit(c)){
                        lexema.append((char) c);
                        estado = 2;
                    }
                    break;
                case 1:
                    if (Character.isLetterOrDigit((char) c) || c == '_') {
                        lexema.append((char) c);
                    } else {
                        this.nextChar = c; //unreadChar();
                        
                        String lexemaFinal = lexema.toString();
                        
                        if(palabrasReservadas.containsKey(lexemaFinal)){
                            return new Token(palabrasReservadas.get(lexemaFinal), lexemaFinal, null, linea);
                        } else {
                            Simbolo s = tablaSimbolos.findOrInsert(lexemaFinal);
                            
                            return new Token(TipoToken.Tipo.IDENTIFICADOR, lexemaFinal, s.id, linea);
                        }
                    }
                    break;
                case 2:
                    if(Character.isDigit((char) c)){
                        lexema.append((char) c);
                    } else if (c == '.') {
                        lexema.append((char) c);
                        estado = 3;
                    } else {
                        this.nextChar = c;

                        try {
                            int valor = Integer.parseInt(lexema.toString());
                            return new Token(TipoToken.Tipo.CONSTANTE_ENTERA, lexema.toString(), valor, linea);
                        } catch (NumberFormatException e) {
                            return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Entero fuera de Rango", linea);
                        }
                    }
                    break;
                case 3:
                    if(Character.isDigit((char) c)){
                        lexema.append((char) c);
                        estado = 31;
                    } else {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Numero Real mal Formado", linea);
                    }
                    break;
                case 31:
                    if(Character.isDigit((char) c)){
                        lexema.append((char) c);
                    } else {
                        this.nextChar = c;

                        try {
                            int valor = Integer.parseInt(lexema.toString());
                            return new Token(TipoToken.Tipo.CONSTANTE_REAL, lexema.toString(), valor, linea);
                        } catch (NumberFormatException e) {
                            return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Numero Real fuera de Rango", linea);
                        }
                    }
                    break;
            }
        }
    }

    private int readChar() throws IOException {
        if(this.nextChar != -1){
            int charDevuelto = this.nextChar;

            this.nextChar = -1;

            return charDevuelto;
        } else {
            return this.reader.read();
        }
    }
}
