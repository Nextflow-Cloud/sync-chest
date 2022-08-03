package cloud.nextflow.syncchest.database.types;

public class H2 implements DatabaseType {
    public String file;
    public String user;
    public String password;

    public H2(String file, String user, String password) {
        this.file = file;
        this.user = user;
        this.password = password;
    }

    @Override
    public DatabaseType getType() {
        return this;
    }

    @Override
    public String toString() {
        return "H2";
    }
}
