package analizadorSintactico;

import analizadorLexico.ALex;
import analizadorLexico.simbolos.*;
import analizadorLexico.tokens.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

public class ASint {
    private final BufferedWriter tokensWriter;
    private final BufferedWriter errorsWriter;
    private ALex alex;
    private int desplActual = 0;
    private Simbolo funcionActual = null;

    private Stack<Object> pila; // Pila Sintáctica
    private Stack<String> pilaSemantica; // Para atributos
    private Stack<Simbolo> pilaLlamadas = new Stack<>(); // Para gestionar llamadas anidadas

    private BufferedWriter parseWriter;
    private final BufferedWriter simbolosWriter;
    private int[][] tablaParsing;

    public ASint(ALex alex, Stack<Object> pila, BufferedWriter parseWriter,
                 BufferedWriter tokensWriter, BufferedWriter errorsWriter, BufferedWriter simbolosWriter) {
        this.alex = alex;
        this.pila = pila;
        this.pilaSemantica = new Stack<>();
        this.parseWriter = parseWriter;
        this.tokensWriter = tokensWriter;
        this.errorsWriter = errorsWriter;
        this.simbolosWriter = simbolosWriter;

        try {
            parseWriter.write("Descendente");
            parseWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        inicializarTabla();
    }

    private Token pedirToken() throws IOException {
        Token token = alex.getNextToken();


        if (token.tipo == TipoToken.Tipo.ERROR) {
            String errorMnsj = "Error Léxico en línea: " + token.linea +
                    " | " + token.atributo + " (Lexema: '" + token.lexema + "')";
            errorsWriter.write(errorMnsj);
            errorsWriter.newLine();
            System.err.println(errorMnsj);
        }
        else {
            tokensWriter.write(token.toFileString());
            tokensWriter.newLine();

            if (token.tipo == TipoToken.Tipo.INT ||
                    token.tipo == TipoToken.Tipo.FLOAT ||
                    token.tipo == TipoToken.Tipo.BOOLEAN ||
                    token.tipo == TipoToken.Tipo.STRING ||
                    token.tipo == TipoToken.Tipo.VOID) {

                pilaSemantica.push(token.lexema);
            }
            // 2. Identificadores (uso o declaración)
            else if (token.tipo == TipoToken.Tipo.IDENTIFICADOR) {
                pilaSemantica.push(token.lexema);
            }
            // 3. Constantes (Expresiones) -> Apilamos su TIPO directamente
            else if (token.tipo == TipoToken.Tipo.CONSTANTE_ENTERA) {
                pilaSemantica.push("int");
            }
            else if (token.tipo == TipoToken.Tipo.CONSTANTE_REAL) {
                pilaSemantica.push("float");
            }
            else if (token.tipo == TipoToken.Tipo.CADENA) {
                pilaSemantica.push("string");
            }
        }

        return token;
    }

    public void analizar() throws IOException {
        pila.push(TipoToken.Tipo.EOF);
        pila.push(NoTerminal.p);

        Token token = pedirToken();

        while(!pila.isEmpty()) {
            Object cima = pila.peek();

            // 1. Si es una ACCIÓN SEMÁNTICA (Marcador)
            if (cima instanceof NoTerminal && ((NoTerminal) cima).name().startsWith("ACCION_")) {
                pila.pop();
                ejecutarAccionSemantica((NoTerminal) cima);
                continue;
            }

            // 2. Si es un TOKEN (Terminal)
            if(cima instanceof TipoToken.Tipo) {
                if(cima == token.tipo){
                    pila.pop();
                    if(cima != TipoToken.Tipo.EOF) token = pedirToken();
                } else {
                    String msgError = "Error sintáctico: Se esperaba " + cima + ", encontrado '" + token.lexema + "'";
                    System.err.println(msgError);
                    errorsWriter.write(msgError);
                    errorsWriter.newLine();
                    return;
                }
            }
            // 3. Si es un NO TERMINAL (Regla)
            else if (cima instanceof NoTerminal) {
                NoTerminal nt = (NoTerminal) cima;
                int regla = tablaParsing[nt.ordinal()][token.tipo.ordinal()];

                if (regla == -1){
                    String descripcion = describirNoTerminal(nt);
                    String msgError = "Error sintáctico en línea " + token.linea +
                            ": Se esperaba " + descripcion +
                            ", pero se encontró '" + token.lexema + "'";

                    System.err.println(msgError);
                    try {
                        errorsWriter.write(msgError);
                        errorsWriter.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                parseWriter.write(" " + regla);
                parseWriter.newLine();

                pila.pop();
                apilarRegla(regla);
            }
        }
    }

    private String describirNoTerminal(NoTerminal nt) {
        switch (nt) {
            case p: return "el programa principal";
            case lUds: return "una lista de declaraciones o sentencias";
            case ud: return "una declaración o sentencia";
            case dVar: return "una declaración de variable (let)";
            case dFunc: return "una declaración de función";
            case tp: return "un tipo de dato (int, float...)";
            case tpRtn: return "un tipo de retorno";
            case lPmts: return "una lista de parámetros";
            case s: return "una sentencia";
            case sS: return "una sentencia simple";
            case sC: return "una sentencia compuesta (if, switch...)";
            case inicID: return "una asignación o llamada a función";
            case sW: return "una instrucción de escritura (write)";
            case sRd: return "una instrucción de lectura (read)";
            case sRtn: return "una sentencia de retorno (return)";
            case sCndS: return "una sentencia condicional (if)";
            case sSw: return "una estructura switch";
            case cSw: return "el cuerpo del switch (casos)";
            case cC: return "un caso (case)";
            case dfltOpc: return "la opción por defecto (default)";
            case e: case eAnd: case eRel: case eCmp: case eArit: case trm: case f:
                return "una expresión";
            case trm_p: case eArit_p:
                return "un operador aritmético o fin de expresión";
            case fIDSuf:
                return "una llamada a función o fin de identificador";
            default:
                return "una estructura válida (" + nt.name() + ")";
        }
    }

    private void ejecutarAccionSemantica(NoTerminal accion) {
        switch (accion) {
            case ACCION_ABRIR_AMBITO:
                alex.getGestorTablas().abrirAmbito();
                desplActual = 0; // Reset para variables locales
                break;
            case ACCION_CERRAR_AMBITO:
                try {
                    alex.getGestorTablas().cerrarAmbito(this.simbolosWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case ACCION_DECLARAR_VAR:
                if (pilaSemantica.size() >= 2) {
                    String id = pilaSemantica.pop();
                    String tipo = pilaSemantica.pop();

                    Simbolo s = alex.getGestorTablas().buscar(id);
                    if (s != null) {
                        s.tipo = tipo;
                        s.categoria = "variable";

                        // calculo desplazamiento
                        int ancho = Simbolo.getAncho(tipo);
                        s.despl = desplActual;
                        desplActual += ancho; // Avanzamos el puntero
                    }
                }
                break;
            case ACCION_BUSCAR_ID:
                if (!pilaSemantica.isEmpty()) {
                    String id = pilaSemantica.pop();
                    Simbolo s = alex.getGestorTablas().buscar(id);

                    if (s == null || s.tipo == null) {
                        System.err.println("Error semántico: Variable '" + id + "' no declarada (Asumimos int global).");
                        try { errorsWriter.write("Error semántico: Variable '" + id + "' no declarada."); errorsWriter.newLine(); } catch(IOException e){}
                        s = alex.getGestorTablas().insertar(id);
                        s.tipo = "int";
                        s.categoria = "variable";
                    }

                    if (s.tipo != null) {
                        pilaSemantica.push(s.tipo);
                    } else {
                        pilaSemantica.push("error");
                    }
                }
                break;
            case ACCION_COMPROBAR_ASIG:
                if (pilaSemantica.size() >= 2) {
                    String tipoExpr = pilaSemantica.pop();
                    String tipoVar = pilaSemantica.pop();


                    if (tipoExpr == null) tipoExpr = "error";
                    if (tipoVar == null) tipoVar = "error";

                    if (!tipoVar.equals(tipoExpr) && !tipoVar.equals("error") && !tipoExpr.equals("error")) {
                        String error = "Error semántico: Asignación incorrecta. Se esperaba '" + tipoVar + "' pero se encontró '" + tipoExpr + "'.";
                        System.err.println(error);
                        try { errorsWriter.write(error); errorsWriter.newLine(); } catch(IOException e) {}
                    }
                }
                break;
            case ACCION_DECLARAR_FUNC:
                if (pilaSemantica.size() >= 2) {
                    String idFunc = pilaSemantica.pop();
                    String tipoRetorno = pilaSemantica.pop();

                    Simbolo s = alex.getGestorTablas().buscar(idFunc);
                    if (s == null) s = alex.getGestorTablas().insertar(idFunc);

                    s.tipo = "funcion";
                    s.categoria = "funcion";
                    s.tipoRetorno = tipoRetorno;
                    s.numParam = 0;
                    s.tiposParam = new ArrayList<>();

                    this.funcionActual = s;
                    this.desplActual = 0;   // Reseteamos desplazamiento

                }
                break;
            case ACCION_DECLARAR_PARAM:
                if (pilaSemantica.size() >= 2) {
                    String id = pilaSemantica.pop();
                    String tipo = pilaSemantica.pop();

                    // 1. Insertar en Tabla Local
                    Simbolo s = alex.getGestorTablas().insertar(id);
                    s.tipo = tipo;
                    s.categoria = "parametro";
                    s.despl = desplActual;
                    desplActual += Simbolo.getAncho(tipo);

                    // 2. Registrar en Función Padre
                    if (this.funcionActual != null) {
                        this.funcionActual.numParam++;
                        this.funcionActual.tiposParam.add(tipo);
                    }
                }
                break;
            case ACCION_COMPROBAR_RETURN:
                if (!pilaSemantica.isEmpty()) {
                    String tipoExpr = pilaSemantica.pop();

                    if (funcionActual != null) {
                        if (!tipoExpr.equals(funcionActual.tipoRetorno) && !tipoExpr.equals("error")) {
                            String err = "Error semántico: La función '" + funcionActual.lexema +
                                    "' debe devolver '" + funcionActual.tipoRetorno +
                                    "' pero devuelve '" + tipoExpr + "'.";
                            System.err.println(err);
                            try { errorsWriter.write(err); errorsWriter.newLine(); } catch(IOException e){}
                        }
                    } else {
                        System.err.println("Error: Return fuera de función (o lógica global no manejada).");
                    }
                }
                break;
            case ACCION_APILAR_VOID:
                pilaSemantica.push("void");
                break;
            case ACCION_USO_VAR:
                // La pila tiene el LEXEMA del ID (puesto por regla 72).
                if (!pilaSemantica.isEmpty()) {
                    String id = pilaSemantica.pop();

                    if (id.equals("true") || id.equals("false")) {
                        pilaSemantica.push("boolean");
                        break;
                    }

                    Simbolo s = alex.getGestorTablas().buscar(id);

                    if (s == null || s.tipo == null) {
                        pilaSemantica.push("error");
                        String err = "Error semántico: La variable '" + id + "' no ha sido declarada.";
                        System.err.println(err);
                        try { errorsWriter.write(err); errorsWriter.newLine(); } catch(IOException e){}
                    } else {
                        pilaSemantica.push(s.tipo);
                    }
                }
                break;

            case ACCION_PREPARAR_LLAMADA:
                if (!pilaSemantica.isEmpty()) {
                    String idFunc = pilaSemantica.pop();
                    Simbolo s = alex.getGestorTablas().buscar(idFunc);
                    if (s == null || !"funcion".equals(s.categoria)) {
                        pilaLlamadas.push(null);
                    } else {
                        pilaLlamadas.push(s);
                    }
                    // Apilamos una "marca" en la pila semántica para saber dónde empiezan los argumentos
                    pilaSemantica.push("MARCA_ARGS");
                }
                break;

            case ACCION_COMPROBAR_ARGS:
                Simbolo funcion = pilaLlamadas.pop();

                Stack<String> argsEncontrados = new Stack<>();
                while (!pilaSemantica.isEmpty()) {
                    String tope = pilaSemantica.pop();
                    if ("MARCA_ARGS".equals(tope)) break; // Encontramos el tope
                    argsEncontrados.push(tope);
                }

                if (funcion != null) {
                    // 1. Verificar número de argumentos
                    if (argsEncontrados.size() != funcion.numParam) {
                        String err = "Error semántico: La función '" + funcion.lexema + "' espera " +
                                funcion.numParam + " argumentos, pero recibió " + argsEncontrados.size() + ".";
                        System.err.println(err);
                        try { errorsWriter.write(err); errorsWriter.newLine(); } catch(IOException e){}
                    }
                    else {
                        // 2. Verificar tipos uno a uno
                        for (int i = 0; i < funcion.numParam; i++) {
                            String tipoEsperado = funcion.tiposParam.get(i);
                            String tipoRecibido = argsEncontrados.pop();

                            if (!tipoRecibido.equals(tipoEsperado) && !tipoRecibido.equals("error")) {
                                String err = "Error semántico: Argumento " + (i+1) + " de '" + funcion.lexema +
                                        "' incorrecto. Esperaba " + tipoEsperado + ", recibió " + tipoRecibido + ".";
                                System.err.println(err);
                                try { errorsWriter.write(err); errorsWriter.newLine(); } catch(IOException e){}
                            }
                        }
                    }

                    pilaSemantica.push(funcion.tipoRetorno);
                } else {
                    pilaSemantica.push("error");
                }
                break;
            case ACCION_OPERADOR_BINARIO:
                if (pilaSemantica.size() >= 2) {
                    String op2 = pilaSemantica.pop();
                    String op1 = pilaSemantica.pop();

                    if (op1.equals("int") && op2.equals("int")) {
                        pilaSemantica.push("int");
                    } else if ((op1.equals("float") || op1.equals("int")) &&
                            (op2.equals("float") || op2.equals("int"))) {
                        pilaSemantica.push("float");
                    } else if (op1.equals("error") || op2.equals("error")) {
                        pilaSemantica.push("error");
                    } else {
                        System.err.println("Error tipos incompatibles en operación: " + op1 + " con " + op2);
                        try { errorsWriter.write("Error tipos: " + op1 + " con " + op2); errorsWriter.newLine(); } catch(IOException e){}
                        pilaSemantica.push("error");
                    }
                }
                break;
        }
    }

    public void inicializarTabla() {
        int numNoTerminales = NoTerminal.values().length;
        int numTerminales = TipoToken.Tipo.values().length;
        
        tablaParsing = new int[numNoTerminales][numTerminales];

        for (int i = 0; i < numNoTerminales; i++) {
            for (int j = 0; j < numTerminales; j++) {
                tablaParsing[i][j] = -1;
            }
        }

        // -----------------------------------------------------------
        // REGLA 1: p -> lUds EOF
        // -----------------------------------------------------------
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.LET.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.FUNCTION.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.IF.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.READ.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 1;
        tablaParsing[NoTerminal.p.ordinal()][TipoToken.Tipo.EOF.ordinal()] = 1;

        // -----------------------------------------------------------
        // REGLA 2: lUds -> ud lUds (Recursiva)
        // -----------------------------------------------------------
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.LET.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.FUNCTION.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.IF.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.READ.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 2;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 2;

        // -----------------------------------------------------------
        // REGLA 3: lUds -> lambda (En EOF y })
        // -----------------------------------------------------------
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.EOF.ordinal()] = 3;
        tablaParsing[NoTerminal.lUds.ordinal()][TipoToken.Tipo.LLAVEDER.ordinal()] = 3;

        // -----------------------------------------------------------
        // REGLA 4, 5, 6: ud -> dVar | dFunc | s
        // -----------------------------------------------------------
        // Regla 4: ud -> dVar
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.LET.ordinal()] = 4;
        // Regla 5: ud -> dFunc
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.FUNCTION.ordinal()] = 5;
        // Regla 6: ud -> s
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.IF.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.READ.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 6;
        tablaParsing[NoTerminal.ud.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 6;

        // -----------------------------------------------------------
        // REGLA 7, 8: s -> sS | sC
        // -----------------------------------------------------------
        // Regla 7: s -> sS (Sentencia Simple)
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 7;
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.READ.ordinal()] = 7;
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 7;
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 7;
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 7;
        // Regla 8: s -> sC (Sentencia Compuesta / SWITCH)
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 8;
        tablaParsing[NoTerminal.s.ordinal()][TipoToken.Tipo.IF.ordinal()] = 8;

        // -----------------------------------------------------------
        // REGLAS DE EXPANSIÓN DE sS (Sentencia Simple)
        // -----------------------------------------------------------
        // Regla 9: sS -> inicID
        tablaParsing[NoTerminal.sS.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 9;
        // Regla 10: sS -> sW
        tablaParsing[NoTerminal.sS.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 10;
        // Regla 11: sS -> sRd
        tablaParsing[NoTerminal.sS.ordinal()][TipoToken.Tipo.READ.ordinal()] = 11;
        // Regla 12: sS -> sRtn
        tablaParsing[NoTerminal.sS.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 12;
        // Regla 13: sS -> sBrk
        tablaParsing[NoTerminal.sS.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 13;

        // -----------------------------------------------------------
        // REGLA 14: sC -> sSw
        // -----------------------------------------------------------
        tablaParsing[NoTerminal.sC.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 14;

        // -----------------------------------------------------------
        // REGLAS DE ID Y ASIGNACIÓN
        // -----------------------------------------------------------
        // Regla 15: inicID -> IDENTIFICADOR restID
        tablaParsing[NoTerminal.inicID.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 15;

        // Regla 16: restID -> opA ... (Asignación)
        tablaParsing[NoTerminal.restID.ordinal()][TipoToken.Tipo.ASIGNACION.ordinal()] = 16;
        tablaParsing[NoTerminal.restID.ordinal()][TipoToken.Tipo.ASIGNACIONMODULO.ordinal()] = 16;

        // Regla 17: restID -> ( ... (Llamada función)
        tablaParsing[NoTerminal.restID.ordinal()][TipoToken.Tipo.PARENTESISIZQ.ordinal()] = 17;

        // Regla 18, 19: opA
        tablaParsing[NoTerminal.opA.ordinal()][TipoToken.Tipo.ASIGNACION.ordinal()] = 18;
        tablaParsing[NoTerminal.opA.ordinal()][TipoToken.Tipo.ASIGNACIONMODULO.ordinal()] = 19;

        // -----------------------------------------------------------
        // REGLAS DE SENTENCIAS ESPECÍFICAS
        // -----------------------------------------------------------
        // Regla 20: sW -> WRITE...
        tablaParsing[NoTerminal.sW.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 20;

        // Regla 21: sRd -> READ...
        tablaParsing[NoTerminal.sRd.ordinal()][TipoToken.Tipo.READ.ordinal()] = 21;

        // Regla 22: sRtn -> RETURN...
        tablaParsing[NoTerminal.sRtn.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 22;

        // Regla 23: sRtn_p -> e (Inicio de expresión)
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.PARENTESISIZQ.ordinal()] = 23;
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 23;
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.CONSTANTE_ENTERA.ordinal()] = 23;
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.CONSTANTE_REAL.ordinal()] = 23;
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.CADENA.ordinal()] = 23;
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.SUMA.ordinal()] = 23;

        // Regla 24: sRtn_p -> lambda
        tablaParsing[NoTerminal.sRtn_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 24;

        // Regla 25: sBrk -> BREAK...
        tablaParsing[NoTerminal.sBrk.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 25;

        // -----------------------------------------------------------
        // REGLAS DE CONTROL (IF / SWITCH)
        // -----------------------------------------------------------
        // Regla 26: sCndS -> IF...
        tablaParsing[NoTerminal.sCndS.ordinal()][TipoToken.Tipo.IF.ordinal()] = 26;

        // Regla 27: sSw -> SWITCH...
        tablaParsing[NoTerminal.sSw.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 27;

        // Regla 28: cSw -> lC dfltOpc
        tablaParsing[NoTerminal.cSw.ordinal()][TipoToken.Tipo.CASE.ordinal()] = 28;
        tablaParsing[NoTerminal.cSw.ordinal()][TipoToken.Tipo.DEFAULT.ordinal()] = 28;
        tablaParsing[NoTerminal.cSw.ordinal()][TipoToken.Tipo.LLAVEDER.ordinal()] = 28;

        // Regla 29: lC -> cC lC
        tablaParsing[NoTerminal.lC.ordinal()][TipoToken.Tipo.CASE.ordinal()] = 29;

        // Regla 30: lC -> lambda
        tablaParsing[NoTerminal.lC.ordinal()][TipoToken.Tipo.DEFAULT.ordinal()] = 30;
        tablaParsing[NoTerminal.lC.ordinal()][TipoToken.Tipo.LLAVEDER.ordinal()] = 30;

        // Regla 31: cC -> CASE...
        tablaParsing[NoTerminal.cC.ordinal()][TipoToken.Tipo.CASE.ordinal()] = 31;

        // Regla 32: bSSw -> s bSSw
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.IF.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.SWITCH.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.BREAK.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.RETURN.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.READ.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.WRITE.ordinal()] = 32;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 32;

        // Regla 33: bSSw -> lambda
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.CASE.ordinal()] = 33;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.DEFAULT.ordinal()] = 33;
        tablaParsing[NoTerminal.bSSw.ordinal()][TipoToken.Tipo.LLAVEDER.ordinal()] = 33;

        // Regla 34: dfltOpc -> DEFAULT...
        tablaParsing[NoTerminal.dfltOpc.ordinal()][TipoToken.Tipo.DEFAULT.ordinal()] = 34;

        // Regla 35: dfltOpc -> lambda
        tablaParsing[NoTerminal.dfltOpc.ordinal()][TipoToken.Tipo.LLAVEDER.ordinal()] = 35;

        // -----------------------------------------------------------
        // REGLAS DE DECLARACIONES
        // -----------------------------------------------------------
        // Regla 36: dVar -> LET...
        tablaParsing[NoTerminal.dVar.ordinal()][TipoToken.Tipo.LET.ordinal()] = 36;

        // Regla 37-40: Tipos
        tablaParsing[NoTerminal.tp.ordinal()][TipoToken.Tipo.INT.ordinal()] = 37;
        tablaParsing[NoTerminal.tp.ordinal()][TipoToken.Tipo.FLOAT.ordinal()] = 38;
        tablaParsing[NoTerminal.tp.ordinal()][TipoToken.Tipo.BOOLEAN.ordinal()] = 39;
        tablaParsing[NoTerminal.tp.ordinal()][TipoToken.Tipo.STRING.ordinal()] = 40;

        // Regla 41: dFunc -> FUNCTION...
        tablaParsing[NoTerminal.dFunc.ordinal()][TipoToken.Tipo.FUNCTION.ordinal()] = 41;

        // Regla 42, 43: tpRtn
        tablaParsing[NoTerminal.tpRtn.ordinal()][TipoToken.Tipo.INT.ordinal()] = 42;
        tablaParsing[NoTerminal.tpRtn.ordinal()][TipoToken.Tipo.FLOAT.ordinal()] = 42;
        tablaParsing[NoTerminal.tpRtn.ordinal()][TipoToken.Tipo.BOOLEAN.ordinal()] = 42;
        tablaParsing[NoTerminal.tpRtn.ordinal()][TipoToken.Tipo.STRING.ordinal()] = 42;
        tablaParsing[NoTerminal.tpRtn.ordinal()][TipoToken.Tipo.VOID.ordinal()] = 43;

        // Regla 44: lPmts -> lPmtsR (Tipos)
        tablaParsing[NoTerminal.lPmts.ordinal()][TipoToken.Tipo.INT.ordinal()] = 44;
        tablaParsing[NoTerminal.lPmts.ordinal()][TipoToken.Tipo.FLOAT.ordinal()] = 44;
        tablaParsing[NoTerminal.lPmts.ordinal()][TipoToken.Tipo.BOOLEAN.ordinal()] = 44;
        tablaParsing[NoTerminal.lPmts.ordinal()][TipoToken.Tipo.STRING.ordinal()] = 44;

        // Regla 45: lPmts -> lambda
        tablaParsing[NoTerminal.lPmts.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 45;

        // Regla 46: lPmtsR -> pmt mPmts
        tablaParsing[NoTerminal.lPmtsR.ordinal()][TipoToken.Tipo.INT.ordinal()] = 46;
        tablaParsing[NoTerminal.lPmtsR.ordinal()][TipoToken.Tipo.FLOAT.ordinal()] = 46;
        tablaParsing[NoTerminal.lPmtsR.ordinal()][TipoToken.Tipo.BOOLEAN.ordinal()] = 46;
        tablaParsing[NoTerminal.lPmtsR.ordinal()][TipoToken.Tipo.STRING.ordinal()] = 46;

        // Regla 47: mPmts -> COMA...
        tablaParsing[NoTerminal.mPmts.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 47;

        // Regla 48: pmt -> tipo ID
        tablaParsing[NoTerminal.pmt.ordinal()][TipoToken.Tipo.INT.ordinal()] = 48;
        tablaParsing[NoTerminal.pmt.ordinal()][TipoToken.Tipo.FLOAT.ordinal()] = 48;
        tablaParsing[NoTerminal.pmt.ordinal()][TipoToken.Tipo.BOOLEAN.ordinal()] = 48;
        tablaParsing[NoTerminal.pmt.ordinal()][TipoToken.Tipo.STRING.ordinal()] = 48;

        // Regla 80 (Salto temporal por error en tu switch): mPmts -> lambda
        tablaParsing[NoTerminal.mPmts.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 80;

        // -----------------------------------------------------------
        // REGLAS DE EXPRESIONES (CASCADA)
        // -----------------------------------------------------------
        // Tokens que inician expresión: (, SUMA, CTEs, CADENA, ID
        int[] iniciosExpresion = {
            TipoToken.Tipo.PARENTESISIZQ.ordinal(),
            TipoToken.Tipo.SUMA.ordinal(),
            TipoToken.Tipo.CONSTANTE_ENTERA.ordinal(),
            TipoToken.Tipo.CONSTANTE_REAL.ordinal(),
            TipoToken.Tipo.CADENA.ordinal(),
            TipoToken.Tipo.IDENTIFICADOR.ordinal()
        };

        // Regla 49: e -> eAnd
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.e.ordinal()][t] = 49;

        // Regla 50: eAnd -> eRel eAnd_p
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.eAnd.ordinal()][t] = 50;

        // Regla 51: eAnd_p -> AND...
        tablaParsing[NoTerminal.eAnd_p.ordinal()][TipoToken.Tipo.AND.ordinal()] = 51;

        // Regla 52: eAnd_p -> lambda
        tablaParsing[NoTerminal.eAnd_p.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 52;
        tablaParsing[NoTerminal.eAnd_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 52;
        tablaParsing[NoTerminal.eAnd_p.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 52;

        // Regla 53: eRel -> eCmp eRel_p
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.eRel.ordinal()][t] = 53;

        // Regla 54: eRel_p -> IGUAL...
        tablaParsing[NoTerminal.eRel_p.ordinal()][TipoToken.Tipo.IGUAL.ordinal()] = 54;

        // Regla 55: eRel_p -> lambda
        tablaParsing[NoTerminal.eRel_p.ordinal()][TipoToken.Tipo.AND.ordinal()] = 55;
        tablaParsing[NoTerminal.eRel_p.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 55;
        tablaParsing[NoTerminal.eRel_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 55;
        tablaParsing[NoTerminal.eRel_p.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 55;

        // Regla 56: eCmp -> eArit eCmp_p
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.eCmp.ordinal()][t] = 56;

        // Regla 57: eCmp_p -> MENORIGUAL...
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.MENORIGUAL.ordinal()] = 57;

        // Regla 58: eCmp_p -> lambda
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.IGUAL.ordinal()] = 58;
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.AND.ordinal()] = 58;
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 58;
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 58;
        tablaParsing[NoTerminal.eCmp_p.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 58;

        // Regla 59: eArit -> trm eArit_p
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.eArit.ordinal()][t] = 59;

        // Regla 60: eArit_p -> SUMA...
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.SUMA.ordinal()] = 60;

        // Regla 61: eArit_p -> lambda
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.MENORIGUAL.ordinal()] = 61;
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.IGUAL.ordinal()] = 61;
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.AND.ordinal()] = 61;
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 61;
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 61;
        tablaParsing[NoTerminal.eArit_p.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 61;

        // Regla 62: trm -> fUnr trm_p
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.trm.ordinal()][t] = 62;

        // Regla 63: trm_p -> opPrd...
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.POR.ordinal()] = 63;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.ENTRE.ordinal()] = 63;

        // Regla 64: trm_p -> lambda
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.SUMA.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.MENORIGUAL.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.IGUAL.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.AND.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.PUNTOYCOMA.ordinal()] = 64;
        tablaParsing[NoTerminal.trm_p.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 64;

        // Regla 65, 66: opProd
        tablaParsing[NoTerminal.opProd.ordinal()][TipoToken.Tipo.POR.ordinal()] = 65;
        tablaParsing[NoTerminal.opProd.ordinal()][TipoToken.Tipo.ENTRE.ordinal()] = 66;

        // Regla 67: fUnr -> SUMA...
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.SUMA.ordinal()] = 67;

        // Regla 68: fUnr -> f
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.PARENTESISIZQ.ordinal()] = 68;
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.CONSTANTE_ENTERA.ordinal()] = 68;
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.CONSTANTE_REAL.ordinal()] = 68;
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.CADENA.ordinal()] = 68;
        tablaParsing[NoTerminal.fUnr.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 68;

        // Regla 69-73: factor (f)
        tablaParsing[NoTerminal.f.ordinal()][TipoToken.Tipo.CONSTANTE_ENTERA.ordinal()] = 69;
        tablaParsing[NoTerminal.f.ordinal()][TipoToken.Tipo.CONSTANTE_REAL.ordinal()] = 70;
        tablaParsing[NoTerminal.f.ordinal()][TipoToken.Tipo.CADENA.ordinal()] = 71;
        tablaParsing[NoTerminal.f.ordinal()][TipoToken.Tipo.IDENTIFICADOR.ordinal()] = 72;
        tablaParsing[NoTerminal.f.ordinal()][TipoToken.Tipo.PARENTESISIZQ.ordinal()] = 73;

        // Regla 74: fIDSuf -> ( ...
        tablaParsing[NoTerminal.fIDSuf.ordinal()][TipoToken.Tipo.PARENTESISIZQ.ordinal()] = 74;

        // Regla 75: fIDSuf -> lambda (Todos los follows de factor)
        int[] followersFactor = {
                TipoToken.Tipo.POR.ordinal(),
                TipoToken.Tipo.ENTRE.ordinal(),
                TipoToken.Tipo.SUMA.ordinal(),
                TipoToken.Tipo.MENORIGUAL.ordinal(),
                TipoToken.Tipo.IGUAL.ordinal(),
                TipoToken.Tipo.AND.ordinal(),
                TipoToken.Tipo.PARENTESISDER.ordinal(),
                TipoToken.Tipo.PUNTOYCOMA.ordinal(),
                TipoToken.Tipo.COMA.ordinal(),
                TipoToken.Tipo.ASIGNACION.ordinal(),
                TipoToken.Tipo.ASIGNACIONMODULO.ordinal(),
                TipoToken.Tipo.LLAVEDER.ordinal(), // Por si acaso
                TipoToken.Tipo.EOF.ordinal()        // Por si acaso
        };

        for (int t : followersFactor) {
            tablaParsing[NoTerminal.fIDSuf.ordinal()][t] = 75;
        }

        // Regla 76: lArgs -> lExps
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.lArgs.ordinal()][t] = 76;

        // Regla 77: lArgs -> lambda
        tablaParsing[NoTerminal.lArgs.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 77;

        // Regla 78: lExps -> e...
        for(int t : iniciosExpresion) tablaParsing[NoTerminal.lExps.ordinal()][t] = 78;

        // Regla 79: mExps -> COMA...
        tablaParsing[NoTerminal.mExps.ordinal()][TipoToken.Tipo.COMA.ordinal()] = 79;

        // Regla 80: mExps -> lambda
        tablaParsing[NoTerminal.mExps.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 80;

        // Regla 81: mPmts -> lambda
        tablaParsing[NoTerminal.mPmts.ordinal()][TipoToken.Tipo.PARENTESISDER.ordinal()] = 81;

        // Regla 82: sC -> sCndS
        tablaParsing[NoTerminal.sC.ordinal()][TipoToken.Tipo.IF.ordinal()] = 82;
    }

    private void apilarRegla(int regla) throws IOException {
        switch (regla) {
            case 1: // p → lUds EOF
                pila.push(TipoToken.Tipo.EOF);
                pila.push(NoTerminal.lUds);
                break;
            case 2: // lUds → ud lUds
                pila.push(NoTerminal.lUds);
                pila.push(NoTerminal.ud);
                break;
            case 3: // lUds → lambda
                break;
            case 4: // ud → dVar
                pila.push(NoTerminal.dVar);
                break;
            case 5: // ud → dFunc
                pila.push(NoTerminal.dFunc);
                break;
            case 6: // ud → s
                pila.push(NoTerminal.s);
                break;
            case 7: // s → sS
                pila.push(NoTerminal.sS);
                break;
            case 8: // s → sC
                pila.push(NoTerminal.sC);
                break;
            case 9: // sS → inicID
                pila.push(NoTerminal.inicID);
                break;
            case 10: // sS → sW
                pila.push(NoTerminal.sW);
                break;
            case 11: // sS → sRd
                pila.push(NoTerminal.sRd);
                break;
            case 12: // sS → sRtn
                pila.push(NoTerminal.sRtn);
                break;
            case 13: // sS → sBrk
                pila.push(NoTerminal.sBrk);
                break;
            case 14: // sC → sSw
                pila.push(NoTerminal.sSw);
                break;
            case 15: // inicID → IDENTIFICADOR ACCION_BUSCAR_ID restID
                pila.push(NoTerminal.restID);
                pila.push(NoTerminal.ACCION_BUSCAR_ID);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                break;

            case 16: // restID → opA e ACCION_COMPROBAR_ASIG PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(NoTerminal.ACCION_COMPROBAR_ASIG);
                pila.push(NoTerminal.e);
                pila.push(NoTerminal.opA);
                break;
            case 17: // restID → PARENTESISIZQ lArgs PARENTESISDER PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.lArgs);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                break;
            case 18: // opA → ASIGNACION
                pila.push(TipoToken.Tipo.ASIGNACION);
                break;
            case 19: // opA → ASIGNACIONMODULO
                pila.push(TipoToken.Tipo.ASIGNACIONMODULO);
                break;
            case 20: // sW → WRITE e PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(NoTerminal.e);
                pila.push(TipoToken.Tipo.WRITE);
                break;
            case 21: // sRd → READ IDENTIFICADOR PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                pila.push(TipoToken.Tipo.READ);
                break;
            case 22: // sRtn → RETURN sRtn_p PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(NoTerminal.sRtn_p);
                pila.push(TipoToken.Tipo.RETURN);
                break;
            case 23: // sRtn_p → e
                pila.push(NoTerminal.ACCION_COMPROBAR_RETURN);
                pila.push(NoTerminal.e);
                break;
            case 24: // sRtn_p → lambda
                pila.push(NoTerminal.ACCION_COMPROBAR_RETURN); // 2. Comprueba que la función sea void
                pila.push(NoTerminal.ACCION_APILAR_VOID);      // 1. Apila "void"
                break;
            case 25: // sBrk → BREAK PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(TipoToken.Tipo.BREAK);
                break;
            case 26: // sCndS → IF PARENTESISIZQ e PARENTESISDER s
                pila.push(NoTerminal.s);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.e);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                pila.push(TipoToken.Tipo.IF);
                break;
            case 27: // sSw → SWITCH PARENTESISIZQ e PARENTESISDER LLAVEIZQ cSw LLAVEDER
                pila.push(TipoToken.Tipo.LLAVEDER);
                pila.push(NoTerminal.cSw);
                pila.push(TipoToken.Tipo.LLAVEIZQ);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.e);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                pila.push(TipoToken.Tipo.SWITCH);
                break;
            case 28: // cSw → lC dfltOpc
                pila.push(NoTerminal.dfltOpc);
                pila.push(NoTerminal.lC);
                break;
            case 29: // lC → cC lC
                pila.push(NoTerminal.lC);
                pila.push(NoTerminal.cC);
                break;
            case 30: // lc → lambda
                break;
            case 31: // cC → CASE CONSTANTE_ENTERA DOSPUNTOS bSSw
                pila.push(NoTerminal.bSSw);
                pila.push(TipoToken.Tipo.DOSPUNTOS);
                pila.push(TipoToken.Tipo.CONSTANTE_ENTERA);
                pila.push(TipoToken.Tipo.CASE);
                break;
            case 32: // bSSw → s bSSw
                pila.push(NoTerminal.bSSw);
                pila.push(NoTerminal.s);
                break;
            case 33: // bSSw → lambda
                break;
            case 34: // dfltOpc → DEFAULT DOSPUNTOS bSSw
                pila.push(NoTerminal.bSSw);
                pila.push(TipoToken.Tipo.DOSPUNTOS);
                pila.push(TipoToken.Tipo.DEFAULT);
                break;
            case 35: // dfltOpc → lambda
                break;
            case 36: // dVar → LET tp IDENTIFICADOR PUNTOYCOMA
                // Orden inverso de apilado:
                // 1. PUNTOYCOMA
                // 2. ACCION_DECLARAR_VAR (¡Ejecutar justo después de leer ID!)
                // 3. IDENTIFICADOR
                // 4. tp
                // 5. LET
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
                pila.push(NoTerminal.ACCION_DECLARAR_VAR);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                pila.push(NoTerminal.tp);
                pila.push(TipoToken.Tipo.LET);
                break;
            case 37: // tp → INT
                pila.push(TipoToken.Tipo.INT);
                break;
            case 38: // tp → FLOAT
                pila.push(TipoToken.Tipo.FLOAT);
                break;
            case 39: // tp → BOOLEAN
                pila.push(TipoToken.Tipo.BOOLEAN);
                break;
            case 40: // tp → STRING
                pila.push(TipoToken.Tipo.STRING);
                break;
            case 41: // dFunc → FUNCTION tpRtn ID ( lPmts ) { lUds }

                pila.push(NoTerminal.ACCION_CERRAR_AMBITO);
                pila.push(TipoToken.Tipo.LLAVEDER);
                pila.push(NoTerminal.lUds);

                pila.push(TipoToken.Tipo.LLAVEIZQ);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.lPmts);

                pila.push(NoTerminal.ACCION_ABRIR_AMBITO);

                pila.push(TipoToken.Tipo.PARENTESISIZQ);

                pila.push(NoTerminal.ACCION_DECLARAR_FUNC);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                pila.push(NoTerminal.tpRtn);
                pila.push(TipoToken.Tipo.FUNCTION);
                break;
            case 42: // tpRtn → tp
                pila.push(NoTerminal.tp);
                break;
            case 43: // tpRtn → VOID
                pila.push(TipoToken.Tipo.VOID);
                break;
            case 44: // lPmts → lPmtsR
                pila.push(NoTerminal.lPmtsR);
                break;
            case 45: // lPmts → lambda
                break;
            case 46: // lPmtsR → pmt mPmts
                pila.push(NoTerminal.mPmts);
                pila.push(NoTerminal.pmt);
                break;
            case 47: // mPmts → COMA pmt mPmts
                pila.push(NoTerminal.mPmts);
                pila.push(NoTerminal.pmt);
                pila.push(TipoToken.Tipo.COMA);
                break;
            case 48: // pmt → tp IDENTIFICADOR
                pila.push(NoTerminal.ACCION_DECLARAR_PARAM);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                pila.push(NoTerminal.tp);
                break;
            case 49: // e → eAnd
                pila.push(NoTerminal.eAnd);
                break;
            case 50: // eAnd → eRel eAnd_p
                pila.push(NoTerminal.eAnd_p);
                pila.push(NoTerminal.eRel);
                break;
            case 51: // eAnd_p → AND eRel eAnd_p
                pila.push(NoTerminal.eAnd_p);
                pila.push(NoTerminal.eRel);
                pila.push(TipoToken.Tipo.AND);
                break;
            case 52: // eAnd_p → lambda
                break;
            case 53: // eRel → eCmp eRel_p
                pila.push(NoTerminal.eRel_p);
                pila.push(NoTerminal.eCmp);
                break;
            case 54: // eRel_p → IGUAL eCmp eRel_p
                pila.push(NoTerminal.eRel_p);
                pila.push(NoTerminal.eCmp);
                pila.push(TipoToken.Tipo.IGUAL);
                break;
            case 55: // eRel_p → lambda
                break;
            case 56: // eCmp → eArit eCmp_p
                pila.push(NoTerminal.eCmp_p);
                pila.push(NoTerminal.eArit);
                break;
            case 57: // eCmp_p → MENORIGUAL eArit eCmp_p
                pila.push(NoTerminal.eCmp_p);
                pila.push(NoTerminal.eArit);
                pila.push(TipoToken.Tipo.MENORIGUAL);
                break;
            case 58: // eCmp_p → lambda
                break;
            case 59: // eArit → trm eArit_p
                pila.push(NoTerminal.eArit_p);
                pila.push(NoTerminal.trm);
                break;
            case 60: // eArit_p → SUMA trm eArit_p
                pila.push(NoTerminal.eArit_p);
                pila.push(NoTerminal.ACCION_OPERADOR_BINARIO);
                pila.push(NoTerminal.trm);
                pila.push(TipoToken.Tipo.SUMA);
                break;
            case 61: // eArit_p → lambda
                break;
            case 62: // trm → fUnr trm_p
                pila.push(NoTerminal.trm_p);
                pila.push(NoTerminal.fUnr);
                break;
            case 63: // trm_p → opPrd fUnr trm_p
                pila.push(NoTerminal.trm_p);
                pila.push(NoTerminal.fUnr);
                pila.push(NoTerminal.opProd);
                break;
            case 64: // trm_p → lambda
                break;
            case 65: // opPrd → POR
                pila.push(TipoToken.Tipo.POR);
                break;
            case 66: // opPrd → ENTRE
                pila.push(TipoToken.Tipo.ENTRE);
                break;
            case 67: // fUnr → SUMA fUnr
                pila.push(NoTerminal.fUnr);
                pila.push(TipoToken.Tipo.SUMA);
                break;
            case 68: // fUnr → f
                pila.push(NoTerminal.f);
                break;
            case 69: // f → CONSTANTE_ENTERA
                pila.push(TipoToken.Tipo.CONSTANTE_ENTERA);
                break;
            case 70: // f → CONSTANTE_REAL
                pila.push(TipoToken.Tipo.CONSTANTE_REAL);
                break;
            case 71: // f → CADENA
                pila.push(TipoToken.Tipo.CADENA);
                break;
            case 72: // f → IDENTIFICADOR fIDSuf
                pila.push(NoTerminal.fIDSuf);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                break;
            case 73: // f → PARENTESISIZQ e PARENTESISDER
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.e);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                break;
            case 74: // fIDSuf → PARENTESISIZQ lArgs PARENTESISDER
                pila.push(NoTerminal.ACCION_COMPROBAR_ARGS);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.lArgs);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                pila.push(NoTerminal.ACCION_PREPARAR_LLAMADA);
                break;
            case 75: // fIDSuf → lambda
                pila.push(NoTerminal.ACCION_USO_VAR);
                break;
            case 76: // lArgs → lExps
                pila.push(NoTerminal.lExps);
                break;
            case 77: // lArgs → lambda
                break;
            case 78: // lExps → e mExps
                pila.push(NoTerminal.mExps);
                pila.push(NoTerminal.e);
                break;
            case 79: // mExps → COMA e mExps
                pila.push(NoTerminal.mExps);
                pila.push(NoTerminal.e);
                pila.push(TipoToken.Tipo.COMA);
                break;
            case 80: // mExps → lambda
                break;
            case 81: // mPmts → lambda
                break;
            case 82: // sC -> sCndS
                pila.push(NoTerminal.sCndS);
                break;
        }
    }
}
