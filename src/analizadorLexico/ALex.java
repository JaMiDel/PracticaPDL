package analizadorLexico;

import analizadorLexico.simbolos.GestorTablas;
import analizadorLexico.simbolos.Simbolo;
import analizadorLexico.tokens.TipoToken;
import analizadorLexico.tokens.Token;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ALex {
    private final BufferedReader reader;
    private int linea;
    private int nextChar;

    private final GestorTablas gestorTablas;
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

    public ALex(GestorTablas gestorTablas, String filePath) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
        this.gestorTablas = gestorTablas;
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
                    else if(Character.isDigit(c)){
                        lexema.append((char) c);
                        estado = 2;
                    }
                    else if(c == '\'') {
                        lexema.setLength(0);
                        estado = 4;
                    }
                    else if (c == '/'){
                        estado = 5;
                    }
                    else if(c == '%'){
                        lexema.append((char) c);
                        estado = 7;
                    }
                    else if(c == '='){
                        lexema.append((char) c);
                        estado = 8;
                    }
                    else if(c == '<'){
                        lexema.append((char) c);
                        estado = 9;
                    }
                    else if(c == '&'){
                        lexema.append((char) c);
                        estado = 10;
                    }
                    else if(c == '+'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.SUMA, lexema.toString(), null, linea);
                    }
                    else if(c == '*'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.POR, lexema.toString(), null, linea);
                    }
                    else if(c == ','){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.COMA, lexema.toString(), null, linea);
                    }
                    else if(c == ';'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.PUNTOYCOMA, lexema.toString(), null, linea);
                    }
                    else if(c == ':'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.DOSPUNTOS, lexema.toString(), null, linea);
                    }
                    else if(c == '('){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.PARENTESISIZQ, lexema.toString(), null, linea);
                    }
                    else if(c == ')'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.PARENTESISDER, lexema.toString(), null, linea);
                    }
                    else if(c == '{'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.LLAVEIZQ, lexema.toString(), null, linea);
                    }
                    else if(c == '}'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.LLAVEDER, lexema.toString(), null, linea);
                    } else {
                        if(c == -1){
                            return new Token(TipoToken.Tipo.EOF, "EOF", null, linea);
                        } else if (Character.isWhitespace(c)) {
                            if(c == '\n') linea++;
                            continue;
                        } else {
                            String lexemaError = String.valueOf((char) c);
                            return new Token(TipoToken.Tipo.ERROR, lexemaError,
                                    "Simbolo '" + lexemaError + "' no reconocido por el lenguaje", linea);
                        }
                    }
                    break;
                case 1:
                    if (Character.isLetterOrDigit((char) c) || c == '_') {
                        lexema.append((char) c);
                    } else {
                        this.nextChar = c;

                        String lexemaFinal = lexema.toString();

                        if(lexemaFinal.length() > 64) {
                            return new Token(TipoToken.Tipo.ERROR, lexemaFinal, "Identificador demasiado largo", linea);
                        }

                        if(palabrasReservadas.containsKey(lexemaFinal)){
                            return new Token(palabrasReservadas.get(lexemaFinal), lexemaFinal, null, linea);
                        } else {
                            // 1. Buscamos si ya existe (visible desde este ámbito)
                            Simbolo s = gestorTablas.buscar(lexemaFinal);

                            // 2. Si no existe, lo insertamos en el ámbito actual
                            if (s == null) {
                                s = gestorTablas.insertar(lexemaFinal);
                            }
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
                            long valor = Integer.parseInt(lexema.toString());
                            if (valor > 32767) {
                                return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Entero fuera de rango", linea);
                            }
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
                            double valor = Double.parseDouble(lexema.toString());
                            if (valor > 2147483647) {
                                return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Numero Real fuera de Rango", linea);
                            }
                            return new Token(TipoToken.Tipo.CONSTANTE_REAL, lexema.toString(), valor, linea);
                        } catch (NumberFormatException e) {
                            return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Numero Real fuera de Rango", linea);
                        }
                    }
                    break;
                case 4:
                    if(c == '\''){
                        String contenidoCadena = lexema.toString();

                        if(contenidoCadena.length() > 64){
                            return new Token(TipoToken.Tipo.ERROR, "'" + contenidoCadena + "'", "Cadena demasiado larga", linea);
                        }

                        String lexemaCompleto = "'" + contenidoCadena + "'";

                        return new Token(TipoToken.Tipo.CADENA, lexemaCompleto, contenidoCadena, linea);
                    } else if (c == -1) {
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Cadena sin cerrar al final del fichero", linea);
                    } else if (c == '\n') {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Salto de linea en medio de una cadena", linea);
                    } else {
                        lexema.append((char) c);
                    }
                    break;
                case 5:
                    if(c == '/'){
                        estado = 6;
                    } else{
                        this.nextChar = c;

                        return new Token(TipoToken.Tipo.ENTRE, "/", null, linea);
                    }
                    break;
                case 6:
                    while((c = readChar()) != '\n' && c != -1){

                    }
                    if(c == '\n'){
                        linea++;
                    }
                    estado = 0;
                    continue;
                case 7:
                    if(c == '='){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.ASIGNACIONMODULO, lexema.toString(), null, linea);
                    } else {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Se esperaba un '=' despues del '%'", linea);
                    }
                case 8:
                    if(c == '='){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.IGUAL, lexema.toString(), null, linea);
                    } else {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ASIGNACION, lexema.toString(), null, linea);
                    }
                case 9:
                    if(c == '='){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.MENORIGUAL, lexema.toString(), null, linea);
                    } else {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Se esperaba un '=' despues del '<'", linea);
                    }
                case 10:
                    if(c == '&'){
                        lexema.append((char) c);
                        return new Token(TipoToken.Tipo.AND, lexema.toString(), null, linea);
                    } else {
                        this.nextChar = c;
                        return new Token(TipoToken.Tipo.ERROR, lexema.toString(), "Se esperaba un '&' despues del '&'", linea);
                    }
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

    public analizadorLexico.simbolos.GestorTablas getGestorTablas() {
        return this.gestorTablas;
    }
}
