package cloud.nextflow.syncchest.database.types.mongo;

import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.bukkit.Bukkit;

public class MongoConnector {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public MongoConnector(MongoDB type) {
        try {
            mongoClient = MongoClients.create(type.uri);
            database = mongoClient.getDatabase(type.database);
            this.collection = database.getCollection(type.collection);
        } catch (MongoClientException exception) {
            exception.printStackTrace();
            Bukkit.getLogger().warning("Failed to connect to the MongoDB database. Details of the exception are given above.");
            return;
        }
        Bukkit.getLogger().info("Connected to the MongoDB database.");
    }

    public MongoClient getClient() {
        return this.mongoClient;
    }

    public MongoDatabase getDatabase() {
        return this.database;
    }

    public MongoCollection<Document> getCollection() {
        return this.collection;
    }

    public void setCollection(String collection) {
        try {
            this.collection = database.getCollection(collection);
        } catch (MongoException exception) {
            exception.printStackTrace();
        }
    }

    public void closeConnection() {
        this.mongoClient.close();
    }
}
