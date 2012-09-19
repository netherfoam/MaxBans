package org.maxgamer.maxbans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
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
			//TODO: Validate name, try match player
			
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
			
			long expires = this.getTime(args);
			if(expires <= 0){
				sender.sendMessage(usage);
				return true;
			}
			
			plugin.getBanManager().tempban(name, sb.toString(), banner, expires);
			return true;
		}
	}
	
	public long getTime(String[] args){
		int modifier = 0;
		
		String arg = args[2].toLowerCase();
		
		if(arg.startsWith("hour")){
			modifier = 3600;
		}
		else if(arg.startsWith("min")){
			modifier = 60;
		}
		else if(arg.startsWith("sec")){
			modifier = 1;
		}
		else if(arg.startsWith("week")){
			modifier = 604800;
		}
		else if(arg.startsWith("day")){
			modifier = 86400;
		}
		else if(arg.startsWith("year")){
			modifier = 31449600;
		}
		else if(arg.startsWith("month")){
			modifier = 18446400;
		}
		
		double time = 0;
		try{
			time = Double.parseDouble(args[1]);
		}
		catch(NumberFormatException e){
		}
		
		return (long) (modifier * time) * 1000;
	}
}
