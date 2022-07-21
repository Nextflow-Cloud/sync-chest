package cloud.nextflow.syncchest;

import cloud.nextflow.syncchest.commands.SyncCommands;
import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.HikariCP;
import cloud.nextflow.syncchest.events.SyncEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class Syncchest extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getCommand("schest").setExecutor(new SyncCommands(this));
        this.getServer().getPluginManager().registerEvents(new SyncEvents(this.getConfig()), this);
        this.getServer().getLogger().info("[SyncChest] Succesfully enabled Sync Chest.");
        HikariCP hikariCP = DatabaseAPI.getHikariCP(getConfig().getString("host"),
                getConfig().getString("database"),
                getConfig().getInt("port"),
                getConfig().getString("username"),
                getConfig().getString("password")
        );
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getServer().getLogger().info("[SyncChest] Succesfully disabled Sync Chest.");
    }
}
