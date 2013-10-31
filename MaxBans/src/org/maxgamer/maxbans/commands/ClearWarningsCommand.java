package org.maxgamer.maxbans.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Warn;

public class ClearWarningsCommand extends CmdSkeleton{
    public ClearWarningsCommand(){
        super("clearwarnings", "maxbans.clearwarnings");
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
			
			plugin.getBanManager().clearWarnings(name);
			String message = Msg.get("announcement.player-warnings-cleared", new String[]{"banner", "name"}, new String[]{banner, name});
			plugin.getBanManager().announce(message);
			plugin.getBanManager().addHistory(name, banner, message);
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}
