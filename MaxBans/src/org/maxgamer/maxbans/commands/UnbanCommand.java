package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;

public class UnbanCommand implements CommandExecutor{
    private MaxBans plugin;
    public UnbanCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String usage = ChatColor.RED + "Usage: /unban <player>";
		
		if(args.length > 0){
			String name = args[0];
			String banner;

			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			Ban ban = plugin.getBanManager().getBan(name);
			IPBan ipBan = plugin.getBanManager().getIPBan(plugin.getBanManager().getIP(name));
			if(ban != null || ipBan != null){
				plugin.getBanManager().unban(name);
				sender.sendMessage(ChatColor.GREEN + "Unbanned " + name);
			}
			else{
				sender.sendMessage(ChatColor.RED + "Could not find a ban for " + name);
				return true;
			}
			
			for(Player p : Bukkit.getOnlinePlayers()){
				p.sendMessage(ChatColor.RED + name + " has been unbanned by " + banner + ".");
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
