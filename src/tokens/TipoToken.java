package tokens;

public class TipoToken {
    public enum Tipo {
        //palabras reservadas
        BOOLEAN(16), BREAK(25), CASE(13), FLOAT(17), FUNCTION(26), IF(15), INT(18), LET(30),
        READ(27), RETURN(29), STRING(19), SWITCH(12), VOID(20), WRITE(28), DEFAULT(14),

        //Identificadores y constantes
        IDENTIFICADOR(1), CONSTANTE_ENTERA(2), CONSTANTE_REAL(3), CADENA(4),

        //OPERADORES
        SUMA(5), POR(6), AND(21), MENORIGUAL(23), IGUAL(24),

        //Simbolos
        PARENTESISIZQ(8), PARENTESISDER(9), LLAVEIZQ(10), LLAVEDER(11), COMA(32), PUNTOYCOMA(33), DOSPUNTOS(34),

        //Asignacion
        ASIGNACION(7), ASIGNACIONMODULO(31),

        //Fin de fichero
        EOF(35),

        //Error
        ERROR(-1);

        public final int codigo;

        Tipo(int codigo){
            this.codigo = codigo;
        }
    }
}
