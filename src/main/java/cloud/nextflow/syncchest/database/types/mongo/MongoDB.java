package cloud.nextflow.syncchest.database.types.mongo;

public class MongoDB implements MongoType {

    public String uri;
    public String database;
    public String collection;

    public MongoDB(String uri, String database, String collection) {
        this.uri = uri;
        this.database = database;
        this.collection = collection;
    }

    public MongoDB getType() {
        return this;
    }

    public String getString() {
        return "MongoDB";
    }
}
