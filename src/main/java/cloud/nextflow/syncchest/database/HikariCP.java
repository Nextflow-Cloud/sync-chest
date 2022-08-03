package cloud.nextflow.syncchest.database;

import cloud.nextflow.syncchest.database.types.DatabaseType;
import cloud.nextflow.syncchest.database.types.H2;
import cloud.nextflow.syncchest.database.types.MariaDB;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HikariCP {
    private HikariDataSource hikariCP;

    public HikariCP(H2 type) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMinimumIdle(20);
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setConnectionTestQuery("SELECT 1");
            hikariConfig.setJdbcUrl("jdbc:h2:" + type.file);
            hikariConfig.addDataSourceProperty("user", type.user);
            hikariConfig.addDataSourceProperty("password", type.password);
            this.hikariCP = new HikariDataSource(hikariConfig);
            if (!this.hikariCP.isClosed()) {
                Bukkit.getLogger().info("Connected to H2 DB");
            } else {
                Bukkit.getLogger().info("Failed to connect to H2 database. Are credentials correct?");
            }
            this.initialize();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
            Bukkit.getLogger().info("Failed to connect to H2 database.");
        }
    }

    public HikariCP(MariaDB type) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMinimumIdle(20);
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setConnectionTestQuery("SELECT 1");
            hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
            hikariConfig.setJdbcUrl("jdbc:mariadb://" + type.host + ":" + type.port + "/" + type.database);
            hikariConfig.addDataSourceProperty("user", type.user);
            hikariConfig.addDataSourceProperty("password", type.password);
            this.hikariCP = new HikariDataSource(hikariConfig);
            if (!this.hikariCP.isClosed()) {
                Bukkit.getLogger().info("Connected to MySQL");
            } else {
                Bukkit.getLogger().warning("Failed to connect to MySQL database. Are credentials correct?");
            }
            this.initialize();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
            Bukkit.getLogger().warning("Failed to connect to MySQL database.");
        }
    }

    public void closeConnections(PreparedStatement preparedStatement, Connection connection, ResultSet resultSet) {
        try {
            if (!connection.isClosed()) {
                if (resultSet != null)
                    resultSet.close();
                if (preparedStatement != null)
                    preparedStatement.close();
                connection.close();
            }
        } catch (SQLException | NullPointerException exception) {
            exception.printStackTrace();
        }
    }

    public void initialize() {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = getHikariCP().getConnection();
            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS sc_ (itemstack LONGBLOB, uuid MEDIUMTEXT)");
            preparedStatement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        } finally {
            closeConnections(preparedStatement, connection, null);
        }
    }

    public boolean updateSyncChest(String uuid, ByteArrayOutputStream data) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        boolean exists = false;
        try {
            connection = getHikariCP().getConnection();
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
            closeConnections(preparedStatement, connection, resultSet);
        }
        if (exists) {
            try {
                connection = getHikariCP().getConnection();
                preparedStatement = connection.prepareStatement("UPDATE sc_ SET itemstack = ? WHERE UUID = ?");
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(data.toByteArray()));
                preparedStatement.setString(2, uuid);
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            } finally {
                closeConnections(preparedStatement, connection, null);
            }
        } else {
            try {
                connection = getHikariCP().getConnection();
                preparedStatement = connection.prepareStatement("INSERT INTO sc_ VALUES (?, ?)");
                preparedStatement.setBinaryStream(1, new ByteArrayInputStream(data.toByteArray()));
                preparedStatement.setString(2, uuid);
                preparedStatement.executeUpdate();
            } catch (SQLException exception) {
                exception.printStackTrace();
            } finally {
                closeConnections(preparedStatement, connection, null);
            }
        }
        return false;
    }

    public ByteArrayInputStream getSyncChest(String uuid) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ByteArrayInputStream result = null;
        try {
            connection = getHikariCP().getConnection();
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
            closeConnections(preparedStatement, connection, resultSet);
        }
        return result;
    }

    public HikariDataSource getHikariCP() {
        return this.hikariCP;
    }
}
