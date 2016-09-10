package org.maxgamer.maxbans.commands;

import org.maxgamer.maxbans.database.DatabaseCore;
import org.bukkit.configuration.ConfigurationSection;
import org.maxgamer.maxbans.commands.bridge.SQLiteBridge;
import java.io.File;
import org.maxgamer.maxbans.database.SQLiteCore;
import java.sql.SQLException;
import org.maxgamer.maxbans.commands.bridge.MySQLBridge;
import org.maxgamer.maxbans.database.Database;
import org.bukkit.ChatColor;
import org.maxgamer.maxbans.database.MySQLCore;
import org.maxgamer.maxbans.commands.bridge.VanillaBridge;
import org.maxgamer.maxbans.util.Formatter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MBExportCommand extends CmdSkeleton
{
    public MBExportCommand() {
        super("mbexport", "maxbans.export");
        this.namePos = -1;
    }
    
    public boolean run(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Formatter.primary + "MaxBans Exporter:");
            sender.sendMessage(Formatter.secondary + "/mbexport vanilla " + Formatter.primary + " - Exports bans to vanilla bans.");
        }
        else if (args[0].equalsIgnoreCase("vanilla")) {
            final VanillaBridge bridge = new VanillaBridge();
            bridge.export();
            sender.sendMessage(Formatter.secondary + "Success.");
        }
        else {
            if (args[0].equalsIgnoreCase("mysql")) {
                if (this.plugin.getDB().getCore() instanceof MySQLCore) {
                    sender.sendMessage(ChatColor.RED + "Database is already MySQL");
                    return true;
                }
                final ConfigurationSection cfg = this.plugin.getConfig().getConfigurationSection("database");
                final String host = cfg.getString("host");
                final String port = cfg.getString("port");
                final String user = cfg.getString("user");
                final String pass = cfg.getString("pass");
                final String name = cfg.getString("name");
                try {
                    final DatabaseCore dbCore = new MySQLCore(host, user, pass, name, port);
                    final Database mysql = new Database(dbCore);
                    final MySQLBridge bridge2 = new MySQLBridge(mysql);
                    bridge2.export();
                    sender.sendMessage(ChatColor.GREEN + "Success - Exported to MySQL " + user + "@" + host + "." + name);
                    return true;
                }
                catch (Database.ConnectionException e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to connect to MySQL " + user + "@" + host + "." + name + ChatColor.DARK_RED + " Reason: " + e.getMessage());
                    return true;
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to export to MySQL " + user + "@" + host + "." + name + ChatColor.DARK_RED + " Reason: " + e2.getMessage());
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("sqlite") || args[0].equalsIgnoreCase("sql") || args[0].equalsIgnoreCase("flatfile")) {
                if (this.plugin.getDB().getCore() instanceof SQLiteCore) {
                    sender.sendMessage(ChatColor.RED + "Database is already SQLite");
                    return true;
                }
                final File file = new File(this.plugin.getDataFolder(), "bans.db");
                try {
                    final DatabaseCore dbCore2 = new SQLiteCore(file);
                    final Database sqlite = new Database(dbCore2);
                    final SQLiteBridge bridge3 = new SQLiteBridge(sqlite);
                    bridge3.export();
                    sender.sendMessage(ChatColor.GREEN + "Success - Exported to SQLite " + file.getPath());
                    return true;
                }
                catch (Database.ConnectionException e3) {
                    e3.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to connect to SQLite " + file.getPath() + " Reason: " + e3.getMessage());
                    return true;
                }
                catch (SQLException e4) {
                    e4.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Failed to export to SQLite " + file.getPath() + " Reason: " + e4.getMessage());
                    return true;
                }
            }
            sender.sendMessage(Formatter.secondary + "Failed.  No known exporter: " + args[0]);
        }
        return true;
    }
}
