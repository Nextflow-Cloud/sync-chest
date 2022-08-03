package cloud.nextflow.syncchest.database.types;

public class MariaDB implements DatabaseType {

    public String host;
    public int port;
    public String database;
    public String user = "";
    public String password = "";

    public MariaDB(String host, int port, String database, String user, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
    }

    @Override
    public DatabaseType getType() {
        return this;
    }

    @Override
    public String toString() {
        return "MariaDB";
    }
}