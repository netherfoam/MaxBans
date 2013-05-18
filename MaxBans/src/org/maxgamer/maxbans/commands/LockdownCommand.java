package org.maxgamer.maxbans.commands;

import java.text.ParseException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class LockdownCommand extends CmdSkeleton{
    private static String defaultReason = "Maintenance";
    
    public LockdownCommand(){
        super("lockdown", "maxbans.lockdown.use");
        namePos = -1;
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		boolean on;
		String reason;
		
		if(args.length > 0){
			try{
				on = Util.parseBoolean(args[0]);
				args[0] = ""; //It was a boolean answer, so we can throw it away from the reason.
			}
			catch(ParseException e){
				on = !plugin.getBanManager().isLockdown();
			}
			
			StringBuilder sb = new StringBuilder();
			for(String s : args){
				if(s.isEmpty()) continue;
				sb.append(s + " ");
			}
			if(sb.length() > 0){
				sb.deleteCharAt(sb.length() - 1);
			}
			reason = sb.toString();
			if(reason.isEmpty()){
				reason = defaultReason;
			}
		}
		else{
			on = !plugin.getBanManager().isLockdown();
			reason = defaultReason;
		}
		plugin.getBanManager().setLockdown(on, reason);
		sender.sendMessage(Formatter.secondary + "Lockdown is now " + (on?"enabled. Reason: " + Formatter.primary + plugin.getBanManager().getLockdownReason() + Formatter.secondary:"disabled") + ".");
		String banner = Util.getName(sender);
		plugin.getBanManager().addHistory(banner, banner, banner + " set lockdown: " + plugin.getBanManager().isLockdown());
		return true;
	}
}
