package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.TempBan;

public class TempBanCommand implements CommandExecutor{
    private MaxBans plugin;
    public TempBanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /ban <player> <time> <time form> [-s] <reason>";
		
		if(args.length < 3){
			sender.sendMessage(usage);
			return true;
		}
		else{
			String name = args[0];
			
			long expires = plugin.getBanManager().getTime(args);
			if(expires <= 0){
				sender.sendMessage(usage);
				return true;
			}
			expires += System.currentTimeMillis();
			
			Ban ban = plugin.getBanManager().getBan(name);
			
			if(ban != null){
				if(ban instanceof TempBan){
					//They're already tempbanned!
					
					TempBan tBan = (TempBan) ban;
					if(tBan.getExpires() > expires){
						//Their old ban lasts longer than this one!
						sender.sendMessage(ChatColor.RED + "That player has a tempban which will last longer than the one you supplied!");
						return true;
					}
					else{
						//Increasing a previous ban, remove the old one first.
						plugin.getBanManager().unban(name);
					}
				}
				else{
					//Already perma banned
					sender.sendMessage(ChatColor.RED + "That player is already banned.");
					return true;
				}
			}
			
			//TODO: Validate name, try match player
			Player player = Bukkit.getPlayer(name);
			
			boolean silent = false;
			StringBuilder sb = new StringBuilder();
			
			if(args[3].equalsIgnoreCase("-s")){
				silent = true;
			}
			
			for(int i = (silent ? 4 : 3); i < args.length; i++){
				sb.append(args[i]);
			}
			
			if(sb.length() < 1){
				sb.append("Misconduct.");
				//TODO: Take misconduct from config
			}
			
			String banner = "console";
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			
			plugin.getBanManager().tempban(name, sb.toString(), banner, expires);
			
			if(player != null && player.isOnline()){
				player.kickPlayer(sb.toString());
			}
			
			if(!silent){
				for(Player p : Bukkit.getOnlinePlayers()){
					p.sendMessage(ChatColor.RED + name + " has been banned ("+plugin.getBanManager().getTimeUntil(expires)+") by " + banner + ". reason: " + sb.toString());
				}
			}
			
			return true;
		}
	}
}
