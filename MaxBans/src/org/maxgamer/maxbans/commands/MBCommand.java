package org.maxgamer.maxbans.commands;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;

public class MBCommand extends CmdSkeleton{
    public MBCommand(){
    	super("mb", null);
    	namePos = -1;
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage(Formatter.primary + "MaxBans Commands:");
		CmdSkeleton[] commands = CmdSkeleton.getCommands();
		Arrays.sort(commands);
		
		for(CmdSkeleton skelly : commands){
			if(skelly.hasPermission(sender)){
				sender.sendMessage(skelly.getUsage() + " - " + Formatter.primary + skelly.getDescription());
			}
		}
		return true;
	}
}
