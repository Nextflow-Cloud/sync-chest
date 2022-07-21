package cloud.nextflow.syncchest.database;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAPI {

    private static Map<String, HikariCP> hikariCP = new HashMap<>();

    public static HikariCP getHikariCP(String host, String database, int port, String user, String password) {
        if (hikariCP.containsKey(database)) {
            return hikariCP.get(database);
        } else {
            HikariCP hikari = new HikariCP(host, database, port, user, password);
            hikari.initialize();
            hikariCP.put(database, hikari);
            return hikari;
        }
    }
}
