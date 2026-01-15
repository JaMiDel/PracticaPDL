package analizadorSintactico;

public enum NoTerminal {
    // Estructura General
    p, lUds, ud,

    // Declaraciones
    dVar, tp, dFunc, tpRtn, lPmts, lPmtsR, mPmts, pmt,

    // Sentencias
    s, sS, sC, opA, sW, sRd, sRtn, sRtn_p, sBrk, sCndS,
    inicID, restID,

    // Switch
    sSw, cSw, lC, cC, bSSw, dfltOpc,

    // Expresiones
    e,
    eAnd, eAnd_p,
    eRel, eRel_p,
    eCmp, eCmp_p,
    eArit, eArit_p,
    trm, trm_p, opProd, fUnr, f, fIDSuf,

    // lista argumentos
    lArgs, lExps, mExps,

    // --- ACCIONES SEMÁNTICAS ---
    ACCION_ABRIR_AMBITO,
    ACCION_CERRAR_AMBITO,
    ACCION_DECLARAR_VAR,

    ACCION_BUSCAR_ID,
    ACCION_COMPROBAR_ASIG,
    ACCION_DECLARAR_FUNC,
    ACCION_DECLARAR_PARAM,
    ACCION_COMPROBAR_RETURN,
    ACCION_APILAR_VOID,
    ACCION_USO_VAR,
    ACCION_PREPARAR_LLAMADA,
    ACCION_COMPROBAR_ARGS,
    ACCION_OPERADOR_BINARIO
}
