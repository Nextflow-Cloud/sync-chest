package cloud.nextflow.syncchest.database.types;

public interface DatabaseType {
    String user = "";
    String password = "";
    DatabaseType getType();
    String toString();
}
