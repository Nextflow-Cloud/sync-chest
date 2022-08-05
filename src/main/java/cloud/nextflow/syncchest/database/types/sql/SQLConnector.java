package cloud.nextflow.syncchest.database.types.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLConnector {
    private HikariDataSource hikariCP;

    public SQLConnector(H2 type) {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setMinimumIdle(20);
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setConnectionTestQuery("SELECT 1");
            hikariConfig.setDriverClassName("org.h2.Driver");
            hikariConfig.setJdbcUrl("jdbc:h2:./" + type.file);
            hikariConfig.addDataSourceProperty("user", type.user);
            hikariConfig.addDataSourceProperty("password", type.password);
            this.hikariCP = new HikariDataSource(hikariConfig);
            if (!this.hikariCP.isClosed()) {
                Bukkit.getLogger().info("Connected to H2 DB");
            } else {
                Bukkit.getLogger().warning("Failed to connect to H2 database. Are credentials correct?");
            }
            this.initialize();
        } catch (NullPointerException exception) {
            exception.printStackTrace();
            Bukkit.getLogger().warning("Failed to connect to H2 database.");
        }
    }

    public SQLConnector(MariaDB type) {
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

    public HikariDataSource getHikariCP() {
        return this.hikariCP;
    }
}
