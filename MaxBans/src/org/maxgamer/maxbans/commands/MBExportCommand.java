package org.maxgamer.maxbans.commands;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.TempBan;
import org.maxgamer.maxbans.banmanager.TempIPBan;

public class MBExportCommand implements CommandExecutor{
    private MaxBans plugin;
    public MBExportCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0){
			sender.sendMessage(plugin.color_primary + "MaxBans Exporter:");
			sender.sendMessage(plugin.color_secondary + "/mbexport vanilla " + plugin.color_primary + " - Exports bans to vanilla bans.");
		}
		else{
			if(args[0].equalsIgnoreCase("vanilla")){
				//Import vanilla.
				for(Entry<String, Ban> entry : plugin.getBanManager().getBans().entrySet()){
					if(entry.getValue() instanceof TempBan){
						//So we skip it in good faith.
						continue;
					}
					
					OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
					if(!p.isBanned()) p.setBanned(true);
				}
				
				for(Entry<String, IPBan> entry : plugin.getBanManager().getIPBans().entrySet()){
					if(entry.getValue() instanceof TempIPBan){
						//So we skip it in good faith.
						continue;
					}
					
					Bukkit.banIP(entry.getKey());
				}
				
				sender.sendMessage(plugin.color_secondary + "Success.");
			}
			else{
				sender.sendMessage(plugin.color_secondary + "Failed.  No known exporter: " + args[0]);
			}
		}
		
		return true;
	}
}
