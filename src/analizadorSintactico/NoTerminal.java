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
    lArgs, lExps, mExps
}
