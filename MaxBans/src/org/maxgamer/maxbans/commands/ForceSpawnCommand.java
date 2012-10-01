package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.maxgamer.maxbans.MaxBans;

public class ForceSpawnCommand implements CommandExecutor{
    private Location spawn;
    private MaxBans plugin;
    public ForceSpawnCommand(MaxBans plugin, Location spawn){
        this.spawn = spawn;
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.forcespawn")){
			sender.sendMessage(plugin.color_secondary + "You don't have permission to do that");
			return true;
		}
		String usage = plugin.color_secondary + "Usage: /forcespawn <player>";
		
		if(args.length > 0){
			String name = args[0];
			String banner;
			
			if(sender instanceof Player){
				banner = ((Player) sender).getName();
			}
			else{
				banner = "Console";
			}
			
			Player p = Bukkit.getPlayer(name);
			if(p != null){
				//Double TP for /back'ers
				p.teleport(spawn, TeleportCause.PLUGIN);
				p.teleport(spawn, TeleportCause.PLUGIN);
				p.sendMessage(plugin.color_secondary + "Forced to the spawn by " + banner);
				sender.sendMessage(plugin.color_primary + "Forced " + plugin.color_secondary + p.getName() + plugin.color_primary + " to the spawn.");
			}
			else{
				sender.sendMessage(plugin.color_primary + "No player found: " + plugin.color_secondary + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
