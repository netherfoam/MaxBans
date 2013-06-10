package org.maxgamer.maxbans.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Mute;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.maxgamer.maxbans.util.Util;

public class ChatListener implements Listener {
    private MaxBans plugin;
    public ChatListener(MaxBans mb){
            plugin = mb;
    }
    @EventHandler(priority = EventPriority.NORMAL) //NORMAL, we dont want to interfere with QUICKSHOP, like the retard making GP, do we?
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player p = event.getPlayer();
        
        Mute mute = plugin.getBanManager().getMute(p.getName());
        if (mute != null) {
        	if(plugin.getBanManager().hasImmunity(p.getName())){
        		return; 
        	}
        	if(mute instanceof TempMute){
        		TempMute tMute = (TempMute) mute;
        		p.sendMessage(ChatColor.RED+"You're muted for another " + Util.getTimeUntil(tMute.getExpires()));
        	}
        	else{
        		p.sendMessage(ChatColor.RED+"You're muted!");
        	}
        	
            event.setCancelled(true);
        }
    }
}
