package org.maxgamer.maxbans.commands.bridge;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class DynamicBanBridge implements Bridge
{
    public void export() {
        throw new NotImplementedException("DynamicBan export is not implemented.");
    }
    
    public void load() {
        final MaxBans plugin = MaxBans.instance;
        final File folder = new File("plugins/DynamicBan/data");
        if (!folder.exists()) {
            System.out.println("Invalid folder!");
        }
        FileConfiguration cfg = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(folder, "banned-players.dat"));
        final Set<String> bannedPlayers = (Set<String>)cfg.getKeys(false);
        System.out.println("Loading " + bannedPlayers.size() + " players.");
        for (final String s : bannedPlayers) {
            plugin.getBanManager().ban(s, cfg.getString(s), "Console");
        }
        cfg = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(folder, "banned-ips.dat"));
        final Set<String> bannedIps = (Set<String>)cfg.getKeys(false);
        System.out.println("Loading " + bannedIps.size() + " players.");
        for (final String s2 : bannedIps) {
            plugin.getBanManager().ipban(s2, cfg.getString(s2), "Console");
        }
        cfg = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(folder, "temp-bans.dat"));
        final Set<String> tempbans = (Set<String>)cfg.getKeys(false);
        System.out.println("Loading " + tempbans.size() + " players.");
        for (final String s3 : tempbans) {
            if (Util.isIP(s3)) {
                plugin.getBanManager().tempipban(s3, "Misconduct", "Console", cfg.getLong(s3) * 1000L);
            }
            else {
                plugin.getBanManager().tempban(s3, "Misconduct", "Console", cfg.getLong(s3) * 1000L);
            }
        }
        cfg = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(folder, "muted-players.dat"));
        final Set<String> mutes = (Set<String>)cfg.getKeys(false);
        System.out.println("Loading " + mutes.size() + " players.");
        for (final String s4 : mutes) {
            plugin.getBanManager().tempmute(s4, "Console", "DynamicBan Mute", cfg.getLong(s4) * 1000L);
        }
    }
}
