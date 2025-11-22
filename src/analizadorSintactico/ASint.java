package analizadorSintactico;

import analizadorLexico.ALex;
import analizadorLexico.tokens.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Stack;

public class ASint {
    private ALex alex;
    private Stack<Object> pila;
    private BufferedWriter parseWriter;

    public ASint(ALex alex, Stack<Object> pila, BufferedWriter parseWriter) {
        this.alex = alex;
        this.pila = pila;
        this.parseWriter = parseWriter;
    }

    public void analizar() throws IOException {
        pila.push(TipoToken.Tipo.EOF);
        pila.push(NoTerminal.p);

        Token token = alex.getNextToken();

        while(!pila.isEmpty()) {
            Object cima = pila.peek();

            if(cima instanceof TipoToken.Tipo) {
                if(cima == token.tipo){
                    pila.pop();
                    if(cima != TipoToken.Tipo.EOF) token = alex.getNextToken();
                } else {
                    System.err.println("Error sintáctico: Se esperaba " + cima + ", encontrado " + token.lexema);
                    return;
                }
            } else if (cima instanceof NoTerminal) {
                NoTerminal nt = (NoTerminal) cima;

                // AQUI LEEREMOS DE LA TABLA
                // int regla = tablaParsing[nt.ordinal()][token.tipo.ordinal()]

                int regla = -1;

                if (regla == -1){
                    System.err.println("Error sintáctico en regla para: " + nt);
                    return;
                }

                parseWriter.write("Descendente " + regla);
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
            case 2: // lUds → ud lUds | lambda
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
        }
    }
}
