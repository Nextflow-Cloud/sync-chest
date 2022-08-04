package cloud.nextflow.syncchest.events;

import cloud.nextflow.syncchest.database.DBUtils;
import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.types.mongo.MongoConnector;
import cloud.nextflow.syncchest.database.types.mongo.MongoDB;
import cloud.nextflow.syncchest.database.types.sql.SQLConnector;
import cloud.nextflow.syncchest.database.types.sql.H2;
import cloud.nextflow.syncchest.database.types.sql.MariaDB;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;

import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SyncEvents implements Listener {
    private FileConfiguration config;
    private File dataDirectory;

    public SyncEvents(FileConfiguration config, File dataDirectory) {
        this.config = config;
        this.dataDirectory = dataDirectory;
    }

    @EventHandler
    public void onPlayerCloseChest(InventoryCloseEvent event) {
        if (event.getView().getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', "&4&lSync Chest &r&8- &a"))) {
            Player player = Bukkit.getPlayer(event.getView().getTitle().split(ChatColor.translateAlternateColorCodes('&', "&4&lSync Chest &r&8- &a"))[1]);
            String uuid = player.getUniqueId().toString();
            ItemStack[] itemStacks = event.getInventory().getContents();
            try {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut);
                out.writeObject(itemStacks);
                String type = this.config.getString("type");
                SQLConnector hikariCP = null;
                MongoConnector mongoConnector = null;
                DBUtils dbUtils = null;
                if (type.equalsIgnoreCase("h2")) {
                    H2 h2Type = new H2(this.dataDirectory.toPath().toString() + "\\" + FilenameUtils.getName(this.config.getString("h2.file")), 
                        this.config.getString("h2.username"), 
                        this.config.getString("h2.password")
                    );
                    hikariCP = DatabaseAPI.getHikariCP(h2Type);
                    dbUtils = new DBUtils(hikariCP);
                } else if (type.equalsIgnoreCase("mariadb")) {
                    MariaDB mariaDBType = new MariaDB(this.config.getString("mariadb.host"),
                        this.config.getInt("mariadb.port"),
                        this.config.getString("mariadb.database"),
                        this.config.getString("mariadb.username"),
                        this.config.getString("mariadb.password")
                    );
                    hikariCP = DatabaseAPI.getHikariCP(mariaDBType);
                    dbUtils = new DBUtils(hikariCP);
                } else if (type.equalsIgnoreCase("mongodb")) {
                    MongoDB mongoDBType = new MongoDB(this.config.getString("mongodb.uri"),
                        this.config.getString("mongodb.database"),
                        this.config.getString("mongodb.collection")
                    );
                    mongoConnector = DatabaseAPI.getMongoConnector(mongoDBType);
                    dbUtils = new DBUtils(mongoConnector);
                }
                dbUtils.updateSyncChest(uuid, byteOut);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
