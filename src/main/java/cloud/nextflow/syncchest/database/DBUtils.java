package cloud.nextflow.syncchest.database;

import cloud.nextflow.syncchest.database.types.mongo.MongoConnector;
import cloud.nextflow.syncchest.database.types.sql.SQLConnector;

import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {
    private MongoConnector mongoConnector;
    private SQLConnector sqlConnector;
    private String type;

    public DBUtils(SQLConnector connector) {
        this.sqlConnector = connector;
        this.type = "SQLCONNECTOR";
    }

    public DBUtils(MongoConnector connector) {
        this.mongoConnector = connector;
        this.type = "MONGOCONNECTOR";
    }

    public String getType() {
        return type;
    }

    public boolean updateSyncChest(String uuid, ByteArrayOutputStream data) {
        if (type.equals("MONGOCONNECTOR")) {
            Document toFind = new Document();
            toFind.append("uuid", uuid);
            Document findIfExists = mongoConnector.getCollection().find(toFind).first();
            if (findIfExists == null) {
                Document toInsert = new Document();
                toInsert.append("uuid", uuid);
                toInsert.append("itemstack", data.toByteArray());
                mongoConnector.getCollection().insertOne(toInsert);
                return true;
            }
            Bson toUpdate = Updates.set("itemstack", data.toByteArray());
            UpdateOptions updateOptions = new UpdateOptions();
            mongoConnector.getCollection().updateOne(toFind, toUpdate, updateOptions);
        } else if (type.equalsIgnoreCase("SQLCONNECTOR")) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            boolean exists = false;
            try {
                connection = sqlConnector.getHikariCP().getConnection();
                preparedStatement = connection.prepareStatement("SELECT * FROM sc_ WHERE UUID = ?");
                preparedStatement.setString(1, uuid);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    exists = true;
                } else {
                    exists = false;
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            } finally {
                sqlConnector.closeConnections(preparedStatement, connection, resultSet);
            }
            if (exists) {
                try {
                    connection = sqlConnector.getHikariCP().getConnection();
                    preparedStatement = connection.prepareStatement("UPDATE sc_ SET itemstack = ? WHERE UUID = ?");
                    preparedStatement.setBinaryStream(1, new ByteArrayInputStream(data.toByteArray()));
                    preparedStatement.setString(2, uuid);
                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                } finally {
                    sqlConnector.closeConnections(preparedStatement, connection, null);
                }
            } else {
                try {
                    connection = sqlConnector.getHikariCP().getConnection();
                    preparedStatement = connection.prepareStatement("INSERT INTO sc_ VALUES (?, ?)");
                    preparedStatement.setBinaryStream(1, new ByteArrayInputStream(data.toByteArray()));
                    preparedStatement.setString(2, uuid);
                    preparedStatement.executeUpdate();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                } finally {
                    sqlConnector.closeConnections(preparedStatement, connection, null);
                }
            }
            return false;
        }
        return false;
    }

    public ByteArrayInputStream getSyncChest(String uuid) {
        if (type.equals("MONGOCONNECTOR")) {
            Document toFind = new Document();
            toFind.append("uuid", uuid);
            Document findIfExits = mongoConnector.getCollection().find(toFind).first();
            if (findIfExits == null) {
                return null;
            }
            Binary binData = ( Binary ) findIfExits.get("itemstack");
            return new ByteArrayInputStream(binData.getData());
        } else if (type.equalsIgnoreCase("SQLCONNECTOR")) {
            Connection connection = null;
            PreparedStatement preparedStatement = null;
            ResultSet resultSet = null;
            ByteArrayInputStream result = null;
            try {
                connection = sqlConnector.getHikariCP().getConnection();
                preparedStatement = connection.prepareStatement("SELECT * FROM sc_ WHERE UUID = ?");
                preparedStatement.setString(1, uuid);
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    result = new ByteArrayInputStream(resultSet.getBytes("itemstack"));
                } else {
                    result = null;
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            } finally {
                sqlConnector.closeConnections(preparedStatement, connection, resultSet);
            }
            return result;
        }
        return null;
    }
}
