package cloud.nextflow.syncchest.database;

import cloud.nextflow.syncchest.database.types.H2;
import cloud.nextflow.syncchest.database.types.MariaDB;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAPI {

    private static Map<String, HikariCP> hikariCP = new HashMap<>();

    public static HikariCP getHikariCP(H2 type) {
        if (hikariCP.containsKey(type.file)) {
            return hikariCP.get(type.file);
        } else {
            HikariCP hikari = new HikariCP(type);
            hikari.initialize();
            hikariCP.put(type.file, hikari);
            return hikari;
        }
    }

    public static HikariCP getHikariCP(MariaDB type) {
        if (hikariCP.containsKey(type.database)) {
            return hikariCP.get(type.database);
        } else {
            HikariCP hikari = new HikariCP(type);
            hikari.initialize();
            hikariCP.put(type.database, hikari);
            return hikari;
        }
    }
}
