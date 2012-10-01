package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;

public class IPBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public IPBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /ban <player> [-s] <reason>";
		
		if(args.length > 0){
			String name = args[0];
			boolean silent = plugin.getBanManager().isSilent(args);
			
			//Build the reason
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
			
			//Get the banners name
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			//Fetch their IP address from history
			String ip = plugin.getBanManager().getIP(name);
			//Ban them
			plugin.getBanManager().ipban(ip, reason, banner);
			
			//Kick them
			Player player = Bukkit.getPlayer(name);
			if(player != null && player.isOnline()){
				player.kickPlayer(sb.toString());
			}
			
			//Notify online players
			if(!silent){
				for(Player p : Bukkit.getOnlinePlayers()){
					p.sendMessage(ChatColor.RED + name + " has been banned by " + banner + ". reason: " + sb.toString());
				}
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
