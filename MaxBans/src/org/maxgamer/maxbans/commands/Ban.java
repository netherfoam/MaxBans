package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;

public class Ban implements CommandExecutor{
    private MaxBans plugin;
    public Ban(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /ban <player> [-s] <reason>";
		
		if(args.length > 0){
			String player = args[0];
			boolean silent = isSilent(args);
			StringBuilder sb = new StringBuilder(20);
			
			for(int i = 1; i < args.length; i++){
				sb.append(args[i]);
			}
			
			//TODO: Take this from the config
			if(sb.length() < 1){
				sb.append("Misconduct.");
			}
			
			String reason = sb.toString();
			
			String banner;
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			plugin.getBanManager().ban(player, reason, banner);
			
			if(silent) return true;
			
			for(Player p : Bukkit.getOnlinePlayers()){
				if(p == null || !p.isOnline()) continue;
				p.sendMessage(ChatColor.RED + player + " has been banned by " + banner + ". reason: " + reason);
			}
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
	
	private boolean isSilent(String[] args){
		if(args == null || args.length <= 0){
			return false;
		}
		for(int i = 0; i < 2 && i < args.length; i++){
			if(args[i].equalsIgnoreCase("-s")){
				//Shuffles down the array
				for(int j = i; j < args.length - 1; i++){
					args[j] = args[j+1];
				}
				args[args.length - 1] = "";
				return true;
			}
		}
		return false;
	}
}
