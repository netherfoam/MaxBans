package org.maxgamer.maxbans.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.util.Util;

public class UnMuteCommand extends CmdSkeleton{
    public UnMuteCommand(){
        super("unmute", "maxbans.unmute");
    }
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length > 0){
			boolean silent = Util.isSilent(args);
			
			String name = args[0];
			if(name.isEmpty()){
				sender.sendMessage(Msg.get("error.no-player-given"));
				return true;
			}
			
			name = plugin.getBanManager().match(name);
			if(name == null){
				name = args[0]; //Use exact name then.
			}
			
			Mute mute = plugin.getBanManager().getMute(name);
			if(mute != null){
				plugin.getBanManager().unmute(name);
				String banner = Util.getName(sender);
				
				String message = Msg.get("announcement.player-was-unmuted", new String[]{"banner", "name"}, new String[]{banner, name});
				plugin.getBanManager().announce(message, silent, sender);
			}
			else{
				sender.sendMessage(Msg.get("error.no-mute-found", "name", name));
			}
			
			return true;
		}
		else{
			sender.sendMessage(getUsage());
			return true;
		}
	}
}
