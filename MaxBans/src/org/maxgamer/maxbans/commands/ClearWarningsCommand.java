package org.maxgamer.maxbans.commands;

import java.util.List;

import org.bukkit.Bukkit;
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
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /clearwarnings <player>";
		
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
				sender.sendMessage(plugin.color_secondary + name + plugin.color_primary + " has no warnings to their name.");
				return true;
			}
			
			plugin.getBanManager().clearWarnings(name);
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				p.sendMessage(plugin.color_primary + "Your previous warnings have been pardoned by " + plugin.color_secondary + banner);
			}
			sender.sendMessage(plugin.color_primary + "Pardoned " + plugin.color_secondary + name + plugin.color_primary + "'s warnings.");
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
