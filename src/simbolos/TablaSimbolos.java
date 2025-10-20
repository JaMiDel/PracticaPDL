package simbolos;

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
}
