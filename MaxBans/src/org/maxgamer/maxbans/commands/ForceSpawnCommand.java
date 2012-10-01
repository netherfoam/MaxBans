package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.maxgamer.maxbans.MaxBans;

public class ForceSpawnCommand implements CommandExecutor{
    private MaxBans plugin;
    private Location spawn;
    public ForceSpawnCommand(MaxBans plugin, Location spawn){
        this.plugin = plugin;
        this.spawn = spawn;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.forcespawn")){
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that");
			return true;
		}
		String usage = ChatColor.RED + "Usage: /forcespawn <player>";
		
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
				p.sendMessage(ChatColor.RED + "Forced to the spawn by " + banner);
				sender.sendMessage(ChatColor.AQUA + "Forced " + ChatColor.RED + p.getName() + ChatColor.AQUA + " to the spawn.");
			}
			else{
				sender.sendMessage(ChatColor.AQUA + "No player found: " + ChatColor.RED + name);
			}
			
			return true;
		}
		else{
			sender.sendMessage(usage);
			return true;
		}
	}
}
