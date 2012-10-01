package org.maxgamer.maxbans.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Warn;

public class ClearWarningsCommand implements CommandExecutor{
    private MaxBans plugin;
    public ClearWarningsCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.clearwarnings")){
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that");
			return true;
		}
		String usage = ChatColor.RED + "Usage: /clearwarnings <player> <reason>";
		
		if(args.length > 0){
			String name = args[0];
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			String banner;
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			List<Warn> warnings = plugin.getBanManager().getWarnings(name);
			
			if(warnings == null || warnings.size() == 0){
				sender.sendMessage(ChatColor.RED + name + ChatColor.AQUA + " has no warnings to their name.");
				return true;
			}
			
			plugin.getBanManager().clearWarnings(name);
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.sendMessage(ChatColor.AQUA + "Your previous warnings have been pardoned by " + ChatColor.RED + banner);
			}
			sender.sendMessage(ChatColor.AQUA + "Pardoned " + ChatColor.RED + name + ChatColor.AQUA + "'s warnings.");
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
