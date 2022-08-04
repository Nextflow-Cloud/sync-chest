package cloud.nextflow.syncchest.database.types.sql;

public class H2 implements SQLType {
    public String file;
    public String user;
    public String password;

    public H2(String file, String user, String password) {
        this.file = file;
        this.user = user;
        this.password = password;
    }

    @Override
    public SQLType getType() {
        return this;
    }

    public String toString() {
        return "H2";
    }
}
