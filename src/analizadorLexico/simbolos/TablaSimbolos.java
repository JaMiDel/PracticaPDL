package analizadorLexico.simbolos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private Map<String, Simbolo> tabla;
    public int idTabla; // El número #1, #2...

    public TablaSimbolos(int id) {
        this.tabla = new HashMap<>();
        this.idTabla = id;
    }

    public Simbolo buscar(String lexema) {
        return tabla.get(lexema);
    }

    public Simbolo insertar(String lexema) {
        Simbolo s = new Simbolo(lexema);
        // Aquí asignarías un ID único si lo necesitas para el token
        tabla.put(lexema, s);
        return s;
    }

    // Formato del PDF formatoTS.pdf
    public void volcarAFichero(BufferedWriter writer) throws IOException {
        // CABECERA: "CONTENIDOS DE LA TABLA # 1 :"
        writer.write("CONTENIDOS DE LA TABLA # " + this.idTabla + " :");
        writer.newLine();

        for (Simbolo s : tabla.values()) {
            // LÍNEA LEXEMA: "* LEXEMA : 'nombre'"
            writer.write("* LEXEMA : '" + s.lexema + "'");
            writer.newLine();

            // LÍNEA ATRIBUTOS (Implementaremos la lógica completa luego)
            writer.write("  ATRIBUTOS :");
            writer.newLine();

            if (s.tipo != null) {
                // OJO: Comillas simples en el valor string
                writer.write("  + tipo : '" + s.tipo + "'");
                writer.newLine();
            }
            // ... resto de atributos ...
        }
        writer.newLine(); // Separación visual
        writer.newLine();
    }
}
