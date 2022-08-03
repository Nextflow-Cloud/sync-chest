package cloud.nextflow.syncchest;

import cloud.nextflow.syncchest.commands.SyncCommands;
import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.HikariCP;
import cloud.nextflow.syncchest.database.types.H2;
import cloud.nextflow.syncchest.database.types.MariaDB;
import cloud.nextflow.syncchest.events.SyncEvents;

import org.bukkit.plugin.java.JavaPlugin;

public final class SyncChest extends JavaPlugin {
    @Override
    public void onEnable() {
        // Plugin startup logic
        String type = getConfig().getString("type");
        HikariCP hikariCP;
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getCommand("schest").setExecutor(new SyncCommands(this));
        this.getServer().getPluginManager().registerEvents(new SyncEvents(this.getConfig()), this);
        this.getServer().getLogger().info("[SyncChest] Succesfully enabled Sync Chest.");
        if (type.equalsIgnoreCase("h2")) {
            H2 h2Type = new H2(getConfig().getString("h2.file"), getConfig().getString("h2.username"), getConfig().getString("h2.password"));
            hikariCP = DatabaseAPI.getHikariCP(h2Type);
        } else if (type.equalsIgnoreCase("mariadb")) {
            MariaDB mariaDBType = new MariaDB(getConfig().getString("host"),
                    getConfig().getInt("port"),
                    getConfig().getString("database"),
                    getConfig().getString("username"),
                    getConfig().getString("password")
            );
            hikariCP = DatabaseAPI.getHikariCP(mariaDBType);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getServer().getLogger().info("[SyncChest] Succesfully disabled Sync Chest.");
    }
}
