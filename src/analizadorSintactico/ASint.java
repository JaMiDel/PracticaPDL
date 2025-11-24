package analizadorSintactico;

import analizadorLexico.ALex;
import analizadorLexico.tokens.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class ASint {
    private final BufferedWriter tokensWriter;
    private final BufferedWriter errorsWriter;
    private ALex alex;
    private Stack<Object> pila;
    private BufferedWriter parseWriter;
    private int[][] tablaParsing;

    public ASint(ALex alex, Stack<Object> pila, BufferedWriter parseWriter,
                 BufferedWriter tokensWriter, BufferedWriter errorsWriter) {
        this.alex = alex;
        this.pila = pila;
        this.parseWriter = parseWriter;
        this.tokensWriter = tokensWriter;
        this.errorsWriter = errorsWriter;

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
        }

        return token;
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

    public void analizar() throws IOException {
        pila.push(TipoToken.Tipo.EOF);
        pila.push(NoTerminal.p);

        Token token = pedirToken();

        while(!pila.isEmpty()) {
            Object cima = pila.peek();

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
            } else if (cima instanceof NoTerminal) {
                NoTerminal nt = (NoTerminal) cima;

                // AQUI LEEREMOS DE LA tablaParsing
                int regla = tablaParsing[nt.ordinal()][token.tipo.ordinal()];

                if (regla == -1){
                    String msgError = "Error sintáctico en regla para: " + nt + ", se encontró: " + token.lexema;
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
            case 15: // inicID → IDENTIFICADOR restID
                pila.push(NoTerminal.restID);
                pila.push(TipoToken.Tipo.IDENTIFICADOR);
                break;
            case 16: // restID → opA e PUNTOYCOMA
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
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
                pila.push(NoTerminal.e);
                break;
            case 24: // sRtn_p → lambda
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
                pila.push(TipoToken.Tipo.PUNTOYCOMA);
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
            case 41: // dFunc → FUNCTION tpRtn IDENTIFICADOR PARENTESISIZQ
                     // lPmts PARENTESISDER LLAVEIZQ lUds LLAVEDER
                pila.push(TipoToken.Tipo.LLAVEDER);
                pila.push(NoTerminal.lUds);
                pila.push(TipoToken.Tipo.LLAVEIZQ);
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.lPmts);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
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
                pila.push(TipoToken.Tipo.PARENTESISDER);
                pila.push(NoTerminal.lArgs);
                pila.push(TipoToken.Tipo.PARENTESISIZQ);
                break;
            case 75: // fIDSuf → lambda
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
