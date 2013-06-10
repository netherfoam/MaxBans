package org.maxgamer.maxbans.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.util.Util;

/**
 * 
 * @author Netherfoam
 *
 */
public class ChatCommandListener extends ListenerSkeleton{
	@EventHandler(priority = EventPriority.NORMAL)
	public void onCommand(PlayerCommandPreprocessEvent e){
		if(e.isCancelled()) return;
		String cmd = e.getMessage().split(" ")[0].replaceFirst("/", "");
		
		if(this.getPlugin().getBanManager().isChatCommand(cmd)){
			Player p = e.getPlayer();
	        Mute mute = this.getPlugin().getBanManager().getMute(p.getName());
	        if (mute != null) {
	        	if(this.getPlugin().getBanManager().hasImmunity(p.getName())){
	        		return; 
	        	}
	        	if(mute instanceof TempMute){
	        		TempMute tMute = (TempMute) mute;
	        		p.sendMessage(ChatColor.RED+"You're muted for another " + Util.getTimeUntil(tMute.getExpires()));
	        	}
	        	else{
	        		p.sendMessage(ChatColor.RED+"You're muted!");
	        	}
	        	
	            e.setCancelled(true);
	        }
		}
	}
}
