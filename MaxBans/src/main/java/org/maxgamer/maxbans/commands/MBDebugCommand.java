package org.maxgamer.maxbans.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MBDebugCommand extends CmdSkeleton
{
    public MBDebugCommand() {
        super("mbdebug", "maxbans.debug");
        this.namePos = -1;
        this.minArgs = 1;
    }
    
    public boolean run(final CommandSender sender, final Command command, final String label, final String[] args) {
        Printable out = null;
        Label_0184: {
            if (args[0].equalsIgnoreCase("file")) {
                final File file = new File(this.plugin.getDataFolder(), "debug.txt");
                try {
                    file.createNewFile();
                    final PrintStream ps = new PrintStream(file);
                    out = new Printable() {
                        public void print(final String s) {
                            ps.println(s);
                        }
                    };
                    ps.close();
                    break Label_0184;
                }
                catch (IOException e) {
                    sender.sendMessage("Failed to create debug file");
                    e.printStackTrace();
                    return true;
                }
            }
            if (args[0].equalsIgnoreCase("console")) {
                out = new Printable() {
                    CommandSender sender = Bukkit.getConsoleSender();
                    
                    public void print(final String s) {
                        this.sender.sendMessage(s);
                    }
                };
            }
            else {
                if (!args[0].equalsIgnoreCase("chat")) {
                    sender.sendMessage("No such output method... " + args[0]);
                    sender.sendMessage("/mbdebug file - Outputs debug info to Server\\plugins\\MaxBans\\debug.txt");
                    sender.sendMessage("/mbdebug chat - Outputs debug info to chat");
                    sender.sendMessage("/mbdebug console - Outputs debug info to Console");
                    return true;
                }
                out = new Printable() {
                    public void print(final String s) {
                        sender.sendMessage(s);
                    }
                };
            }
        }
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Date date = new Date();
        System.out.println();
        out.print("MaxBans Data Dump - " + dateFormat.format(date));
        out.print("Bukkit: " + Bukkit.getServer().getBukkitVersion());
        out.print("Plugin: " + this.plugin.toString());
        out.print("IP: " + Bukkit.getIp() + ":" + Bukkit.getPort());
        out.print("Server: " + Bukkit.getServerName());
        out.print("Online-mode: " + Bukkit.getServer().getOnlineMode());
        out.print("Server String: " + Bukkit.getServer().toString());
        out.print("Package: " + Bukkit.getServer().getClass().getCanonicalName());
        out.print("=== Config File ===");
        final String pass = this.plugin.getConfig().getString("database.pass");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pass.length(); ++i) {
            sb.append('*');
        }
        this.plugin.getConfig().set("database.pass", (Object)sb.toString());
        final String syncpass = this.plugin.getConfig().getString("sync.pass");
        sb = new StringBuilder();
        for (int j = 0; j < syncpass.length(); ++j) {
            sb.append('*');
        }
        this.plugin.getConfig().set("sync.pass", (Object)sb.toString());
        out.print(this.plugin.getConfig().saveToString());
        this.plugin.getConfig().set("database.pass", (Object)pass);
        this.plugin.getConfig().set("sync.pass", (Object)pass);
        out.print("=== Vanilla Bans (MaxBans does not handle these) ===");
        out.print(Bukkit.getBannedPlayers());
        out.print("=== Online Players ===");
        out.print(Bukkit.getOnlinePlayers());
        out.print("=== Plugins ===");
        out.print(Bukkit.getPluginManager().getPlugins());
        out.print("=== Whitelisted Players ===");
        out.print(Bukkit.getWhitelistedPlayers());
        out.print("=== Immune Players ===");
        out.print(this.plugin.getBanManager().getImmunities());
        out.print("=== Worlds ===");
        out.print(Bukkit.getWorlds());
        out.print("=== Bans ===");
        out.print(this.plugin.getBanManager().getBans());
        out.print("=== IP Bans ===");
        out.print(this.plugin.getBanManager().getIPBans());
        out.print("=== Mutes ===");
        out.print(this.plugin.getBanManager().getMutes());
        out.print("=== Temp Bans ===");
        out.print(this.plugin.getBanManager().getTempBans());
        out.print("=== Temp IP Bans ===");
        out.print(this.plugin.getBanManager().getTempIPBans());
        out.print("=== Temp Mutes ===");
        out.print(this.plugin.getBanManager().getTempMutes());
        out.print("=== IP Recordings ===");
        out.print(this.plugin.getBanManager().getIPHistory());
        out.print("=== Player Names ===");
        out.print(this.plugin.getBanManager().getPlayers());
        out.print("=== RangeBans ===");
        out.print(this.plugin.getBanManager().getRangeBans());
        out.print("=== IP Whitelist ===");
        out.print(this.plugin.getBanManager().getWhitelist());
        out.print("=== History (Top=Recent) ===");
        out.print(this.plugin.getBanManager().getHistory());
        if (this.plugin.getBanManager().getDNSBL() != null) {
            out.print("=== DNSBL Records ===");
            out.print(this.plugin.getBanManager().getDNSBL().getHistory());
            out.print("=== DNSBL Servers ===");
            out.print(this.plugin.getBanManager().getDNSBL().getServers());
        }
        return true;
    }
    
    public abstract class Printable
    {
        public abstract void print(final String p0);
        
        public void print(final Map<?, ?> map) {
            this.print("Map: " + map.getClass().getCanonicalName() + ", Size: " + map.size());
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                final String s = String.format("%-16s | %.1000s", String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                this.print(s);
            }
        }
        
        public void print(final Collection<?> list) {
            this.print("Collection: " + list.getClass().getCanonicalName() + ", Size: " + list.size());
            for (final Object value : list) {
                this.print(String.valueOf(value));
            }
        }
        
        public <T> void print(final T[] array) {
            this.print("List: " + array.getClass().getCanonicalName() + ", Length: " + array.length);
            for (final T t : array) {
                this.print(String.valueOf(t));
            }
        }
    }
}
