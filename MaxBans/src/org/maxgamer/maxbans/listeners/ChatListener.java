package org.maxgamer.maxbans.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.maxbans.MaxBans;

public class ChatListener implements Listener {
        private MaxBans plugin;
        public ChatListener(MaxBans mb){
                plugin=mb;
        }
        @EventHandler(priority = EventPriority.LOWEST) //Shouldnt this be the highest?
        public void onPlayerChat(AsyncPlayerChatEvent event) {
            Player p = event.getPlayer();
            if (plugin.getBanManager().getMute(p.getName())!=null) {
                event.setCancelled(true);
                p.sendMessage(ChatColor.RED+"You are muted!");
            }
    }
}
