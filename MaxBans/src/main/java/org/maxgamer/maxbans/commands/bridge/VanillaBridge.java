package org.maxgamer.maxbans.commands.bridge;

import org.bukkit.OfflinePlayer;

import org.maxgamer.maxbans.banmanager.TempIPBan;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.bukkit.Bukkit;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.Ban;

import java.util.Map;

import org.maxgamer.maxbans.MaxBans;

public class VanillaBridge implements Bridge
{
    @SuppressWarnings("deprecation")
	public void export() {
        System.out.println("Exporting to Vanilla bans...");
        final MaxBans plugin = MaxBans.instance;
        for (final Map.Entry<String, Ban> entry : plugin.getBanManager().getBans().entrySet()) {
            if (entry.getValue() instanceof TempBan) {
                continue;
            }
            final OfflinePlayer p = Bukkit.getOfflinePlayer((String)entry.getKey());
            if (p.isBanned()) {
                continue;
            }            
            p.setBanned(true);
        }
        for (final Map.Entry<String, IPBan> entry2 : plugin.getBanManager().getIPBans().entrySet()) {
            if (entry2.getValue() instanceof TempIPBan) {
                continue;
            }
            Bukkit.banIP((String)entry2.getKey());
        }
    }
    
    public void load() {
        System.out.println("Importing from Vanilla bans...");
        final MaxBans plugin = MaxBans.instance;
        for (final OfflinePlayer p : Bukkit.getBannedPlayers()) {
            plugin.getBanManager().ban(p.getName(), "Vanilla Ban", "Console");
        }
        for (final String ip : Bukkit.getIPBans()) {
            plugin.getBanManager().ipban(ip, "Vanilla IP Ban", "Console");
        }
    }
}
