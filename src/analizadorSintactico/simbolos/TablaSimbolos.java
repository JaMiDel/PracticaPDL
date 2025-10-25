package analizadorSintactico.simbolos;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, Simbolo> table;
    private int nextId;

    public TablaSimbolos() {
        table = new HashMap<>();
        nextId = 0;
    }

    public Simbolo findOrInsert(String lexema) {
        if (table.containsKey(lexema)) {
            return table.get(lexema);
        } else {
            Simbolo nuevoSimbolo = new Simbolo(lexema, nextId);
            table.put(lexema, nuevoSimbolo);
            nextId++;

            return nuevoSimbolo;
        }
    }

    public void volcarAFichero(String fichero) throws IOException{
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(fichero))){
            writer.write("--- Tabla de Símbolos ---");
            writer.newLine();
            writer.newLine();

            for(Simbolo s : table.values()){
                writer.write("  > " + s.toString());
                writer.newLine();
            }
        }
    }
}
