package cloud.nextflow.syncchest;

import cloud.nextflow.syncchest.commands.SyncCommands;
import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.types.mongo.MongoConnector;
import cloud.nextflow.syncchest.database.types.mongo.MongoDB;
import cloud.nextflow.syncchest.database.types.sql.SQLConnector;
import cloud.nextflow.syncchest.database.types.sql.H2;
import cloud.nextflow.syncchest.database.types.sql.MariaDB;
import cloud.nextflow.syncchest.events.SyncEvents;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class SyncChest extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        String type = getConfig().getString("type");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getCommand("schest").setExecutor(new SyncCommands(this));
        this.getServer().getPluginManager().registerEvents(new SyncEvents(this.getConfig(), this.getDataFolder()), this);
        this.getServer().getLogger().info("[SyncChest] Succesfully enabled Sync Chest.");
        if (type.equalsIgnoreCase("h2")) {
            H2 h2Type = new H2(getDataFolder().toPath().toString() + "\\" + FilenameUtils.getName(getConfig().getString("h2.file")), getConfig().getString("h2.username"), getConfig().getString("h2.password"));
            DatabaseAPI.getHikariCP(h2Type);
        } else if (type.equalsIgnoreCase("mariadb")) {
            MariaDB mariaDBType = new MariaDB(getConfig().getString("mariadb.host"),
                    getConfig().getInt("mariadb.port"),
                    getConfig().getString("mariadb.database"),
                    getConfig().getString("mariadb.username"),
                    getConfig().getString("mariadb.password")
            );
            DatabaseAPI.getHikariCP(mariaDBType);
        } else if (type.equalsIgnoreCase("mongodb")) {
            MongoDB mongoDBType = new MongoDB(getConfig().getString("mongodb.uri"),
                    getConfig().getString("mongodb.database"),
                    getConfig().getString("mongodb.collection")
            );
            DatabaseAPI.getMongoConnector(mongoDBType);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getServer().getLogger().info("[SyncChest] Succesfully disabled Sync Chest.");
    }
}
