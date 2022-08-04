package cloud.nextflow.syncchest.database.types.mongo;

public interface MongoType {
    String uri = "";
    String database = "";
    String collection = "";
    MongoType getType();
}
