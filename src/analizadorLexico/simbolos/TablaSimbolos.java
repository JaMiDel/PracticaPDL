package analizadorLexico.simbolos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, Simbolo> tabla;
    public int idTabla;

    public TablaSimbolos(int id) {
        this.tabla = new HashMap<>();
        this.idTabla = id;
    }

    public Simbolo buscar(String lexema) {
        return tabla.get(lexema);
    }

    public Simbolo insertar(String lexema) {
        Simbolo s = new Simbolo(lexema);
        tabla.put(lexema, s);
        return s;
    }

    public void volcarAFichero(BufferedWriter writer) throws IOException {
        writer.write("TABLA # " + this.idTabla + " :");
        writer.newLine();

        for (Simbolo s : tabla.values()) {
            writer.write("* LEXEMA : '" + s.lexema + "'");
            writer.newLine();
            writer.write("  ATRIBUTOS :");
            writer.newLine();

            // 1. TIPO
            if (s.tipo != null) {
                writer.write("  + tipo : '" + s.tipo + "'");
                writer.newLine();
            }

            // 2. DESPLAZAMIENTO (Solo para variables y parámetros)
            if ("variable".equals(s.categoria) || "parametro".equals(s.categoria)) {
                writer.write("  + despl : " + s.despl);
                writer.newLine();
            }

            // 3. NUM PARAM (Solo funciones)
            if ("funcion".equals(s.categoria)) {
                writer.write("  + numParam : " + s.numParam);
                writer.newLine();
                writer.write("  + tipoRetorno : '" + s.tipoRetorno + "'");
                writer.newLine();

                for (int i = 0; i < s.tiposParam.size(); i++) {
                    writer.write("  + TipoParam" + (i + 1) + " : '" + s.tiposParam.get(i) + "'");
                    writer.newLine();
                }
            }
        }
        writer.newLine(); writer.newLine();
    }
}
