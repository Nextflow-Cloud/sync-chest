package cloud.nextflow.syncchest.database;

import cloud.nextflow.syncchest.database.types.mongo.MongoConnector;
import cloud.nextflow.syncchest.database.types.mongo.MongoDB;
import cloud.nextflow.syncchest.database.types.sql.H2;
import cloud.nextflow.syncchest.database.types.sql.SQLConnector;
import cloud.nextflow.syncchest.database.types.sql.MariaDB;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAPI {
    private static Map<String, SQLConnector> hikariCP = new HashMap<>();
    private static Map<String, MongoConnector> mongoConnector = new HashMap<>();

    public static SQLConnector getHikariCP(H2 type) {
        if (hikariCP.containsKey(type.file)) {
            return hikariCP.get(type.file);
        } else {
            SQLConnector hikari = new SQLConnector(type);
            hikari.initialize();
            hikariCP.put(type.file, hikari);
            return hikari;
        }
    }

    public static SQLConnector getHikariCP(MariaDB type) {
        if (hikariCP.containsKey(type.database)) {
            return hikariCP.get(type.database);
        } else {
            SQLConnector hikari = new SQLConnector(type);
            hikari.initialize();
            hikariCP.put(type.database, hikari);
            return hikari;
        }
    }

    public static MongoConnector getMongoConnector(MongoDB type) {
        if (hikariCP.containsKey(type.database)) {
            return mongoConnector.get(type.database);
        } else {
            MongoConnector mongo = new MongoConnector(type);
            mongoConnector.put(type.database, mongo);
            return mongo;
        }
    }
}
