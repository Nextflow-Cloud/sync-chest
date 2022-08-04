package cloud.nextflow.syncchest.database.types.sql;

public class MariaDB implements SQLType {
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
    public SQLType getType() {
        return this;
    }

    @Override
    public String toString() {
        return "MariaDB";
    }
}
