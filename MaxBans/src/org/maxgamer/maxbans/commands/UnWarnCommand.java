package org.maxgamer.maxbans.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.sync.Packet;

public class UnWarnCommand extends CmdSkeleton{
    public UnWarnCommand(){
        super("unwarn", "maxbans.unwarn");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
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
				sender.sendMessage(Msg.get("error.no-warnings", new String[]{"name"}, new String[]{name}));
				return true;
			}
			
			plugin.getBanManager().deleteWarning(name, warnings.get(warnings.size() - 1));
			
			String message = Msg.get("announcement.unwarn-success", new String[]{"banner", "name"}, new String[]{banner, name});
			plugin.getBanManager().announce(message);
			plugin.getBanManager().addHistory(name, banner, message);
			
			if(plugin.getSyncer() != null){
				//Send the clearwarnings request
	    		Packet prop = new Packet();
	    		prop.setCommand("unwarn");
	    		prop.put("name", name);
	    		prop.put("banner", banner);
	    		plugin.getSyncer().broadcast(prop);
	    		
	    		//Send the addhistory request.
	    		Packet history = new Packet().setCommand("addhistory").put("string", message).put("banner", banner).put("name", name);
	    		plugin.getSyncer().broadcast(history);
	    	}
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}
