package cloud.nextflow.syncchest.commands;

import cloud.nextflow.syncchest.database.DBUtils;
import cloud.nextflow.syncchest.database.DatabaseAPI;
import cloud.nextflow.syncchest.database.types.mongo.MongoConnector;
import cloud.nextflow.syncchest.database.types.mongo.MongoDB;
import cloud.nextflow.syncchest.database.types.sql.SQLConnector;
import cloud.nextflow.syncchest.database.types.sql.H2;
import cloud.nextflow.syncchest.database.types.sql.MariaDB;

import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SyncCommands implements TabExecutor {
    private FileConfiguration config;
    private JavaPlugin plugin;
    private File dataDirectory;

    public SyncCommands(JavaPlugin plugin) {
        this.config = plugin.getConfig();
        this.plugin = plugin;
        this.dataDirectory = plugin.getDataFolder();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player;
            if (args.length == 0) {
                player = (Player)sender;
            } else {
                player = Bukkit.getPlayer(args[0]);
                if (player != null && !player.getUniqueId().toString().equals((( Player ) sender).getUniqueId().toString())) {
                    if (!sender.hasPermission("schest.other") || !sender.hasPermission("schest.admin") || !sender.isOp()) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + " &cI'm sorry but you don't have permission to execute this command."));
                        return false;
                    }
                } else {
                    switch (args[0]) {
                        case "help":
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8----- &4&lSync Chest &8-----"));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7<> are optional arguments."));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/schest &8- &9View your synchronised chest."));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/schest help &8- &9Get this menu again."));
                            if (sender.hasPermission("schest.other") || sender.hasPermission("schest.admin") || sender.isOp()) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/schest <Player name> &8- &9View another persons chest."));
                            }
                            if (sender.hasPermission("schest.admin") || sender.isOp())
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&9/schest reload &8- &9Reload the plugins configuration file."));
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8----- &4&lSync Chest &8-----"));
                            return true;
                        case "reload":
                            plugin.reloadConfig();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + " &aReloaded the configuration files."));
                            return true;
                        case "update":
                            File currentPlugin = null;
                            try {
                                currentPlugin = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
                                File newFile = new File(currentPlugin.getName() + 'n');
                                newFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            try (InputStream in = new URL("").openStream()) {
                                Files.copy(in, Paths.get(currentPlugin.getName()));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        default:
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.config.getString("prefix") + " &cSorry could not find that player/command."));
                            return false;
                    }
                }
            }
            String uuid = player.getUniqueId().toString();
            Inventory schest = Bukkit.createInventory(((Player)sender), 54, ChatColor.translateAlternateColorCodes('&', "&4&lSync Chest &r&8- &a" + player.getName()));
            String type = this.config.getString("type");
            SQLConnector hikariCP = null;
            MongoConnector mongoConnector = null;
            DBUtils dbUtils = null;

            if (type.equalsIgnoreCase("h2")) {
                H2 h2Type = new H2(this.dataDirectory.toPath().toString() + "\\" + FilenameUtils.getName(this.config.getString("h2.file")), this.config.getString("h2.username"), this.config.getString("h2.password"));
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

            ItemStack[] itemStacks = null;
            try {
                ByteArrayInputStream byteIn = dbUtils.getSyncChest(uuid);
                if (byteIn != null) {
                    BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn);
                    itemStacks = (ItemStack[]) in.readObject();
                    schest.setContents(itemStacks);
                }
            } catch (IOException | ClassNotFoundException exception) {
                exception.printStackTrace();
            }
            ((Player)sender).openInventory(schest);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> tab = new ArrayList<>();
        tab.add("reload");
        tab.add("help");
        return tab;
    }
}
