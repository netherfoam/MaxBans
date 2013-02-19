package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class MBImportCommand extends CmdSkeleton{
    public MBImportCommand(){
        super("mbimport", "maxbans.import");
        namePos = -1;
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0){
			sender.sendMessage(Formatter.primary + "MaxBans Importer:");
			sender.sendMessage(Formatter.secondary + "/mbimport vanilla " + Formatter.primary + " - Imports vanilla bans.");
		}
		else{
			if(args[0].equalsIgnoreCase("vanilla")){
				//Import vanilla.
				for(OfflinePlayer p : Bukkit.getBannedPlayers()){
					plugin.getBanManager().ban(p.getName(), "Vanilla Ban", "Console");
				}
				for(String ip : Bukkit.getIPBans()){
					plugin.getBanManager().ipban(ip, "Vanilla IP Ban", "Console");
				}
				
				sender.sendMessage(Formatter.secondary + "Success.");
			}
			else{
				sender.sendMessage(Formatter.secondary + "Failed.  No known importer: " + args[0]);
			}
		}
		
		return true;
	}
}
