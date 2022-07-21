package cloud.nextflow.syncchest.events;

import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.HikariCP;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class SyncEvents implements Listener {

    private FileConfiguration config;

    public SyncEvents(FileConfiguration plugin) {
        this.config = plugin;
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

                HikariCP hikariCP = DatabaseAPI.getHikariCP(this.config.getString("host"),
                        this.config.getString("database"),
                        this.config.getInt("port"),
                        this.config.getString("username"),
                        this.config.getString("password")
                );
                hikariCP.updateSyncChest(uuid, byteOut);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}
