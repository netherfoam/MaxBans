package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.banmanager.HistoryRecord;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class HistoryCommand extends CmdSkeleton{
    public HistoryCommand(){
        super("maxbans.history");
        usage = Formatter.secondary + "Usage: /history <number of records>";
        namePos = -1;
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		int count;
		if(args.length < 1) count = 20;
		else{
			try{
				count = Integer.parseInt(args[0]);				
			}
			catch(NumberFormatException e){
				sender.sendMessage(Formatter.secondary + usage);
				return true;
			}
		}
		HistoryRecord[] history = plugin.getBanManager().getHistory();
		if(history.length <= 0){
			sender.sendMessage(Formatter.primary + "No history.");
		}
		else{
			for(int i = Math.min(history.length, count) - 1; i >= 0; i--){
				sender.sendMessage(Formatter.primary + "[" + Util.getShortTime(System.currentTimeMillis() - history[i].getCreated()) + "] " + Formatter.secondary + history[i].getMessage());
			}
		}
		return true;
	}
}
