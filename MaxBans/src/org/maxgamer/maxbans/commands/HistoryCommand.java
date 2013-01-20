package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.HistoryRecord;
import org.maxgamer.maxbans.util.Formatter;

public class HistoryCommand implements CommandExecutor{
    private MaxBans plugin;
    public HistoryCommand(MaxBans plugin){
        this.plugin = plugin;
    }
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("maxbans.history")){
			sender.sendMessage(Formatter.secondary + "You don't have permission to do that");
			return true;
		}
		int count;
		if(args.length < 1) count = 20;
		else{
			try{
				count = Integer.parseInt(args[0]);				
			}
			catch(NumberFormatException e){
				sender.sendMessage(Formatter.secondary + "Usage: /history <number fo records>");
				return true;
			}
		}
		HistoryRecord[] history = plugin.getBanManager().getHistory();
		if(history.length <= 0){
			sender.sendMessage(Formatter.primary + "No history.");
		}
		else{
			for(int i = 0; i < count && i < history.length; i++){
				sender.sendMessage(Formatter.secondary + history[i].getMessage());
			}
		}
		return true;
	}
}
