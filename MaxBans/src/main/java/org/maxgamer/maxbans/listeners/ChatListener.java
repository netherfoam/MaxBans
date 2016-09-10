package org.maxgamer.maxbans.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.maxgamer.maxbans.banmanager.Mute;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.ChatColor;
import org.maxgamer.maxbans.banmanager.TempMute;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.maxgamer.maxbans.MaxBans;
import org.bukkit.event.Listener;

public class ChatListener implements Listener
{
    private MaxBans plugin;
    
    public ChatListener(final MaxBans mb) {
        super();
        this.plugin = mb;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player p = event.getPlayer();
        final Mute mute = this.plugin.getBanManager().getMute(p.getName());
        if (mute != null) {
            if (this.plugin.getBanManager().hasImmunity(p.getName())) {
                return;
            }
            if (mute instanceof TempMute) {
                final TempMute tMute = (TempMute)mute;
                p.sendMessage(ChatColor.RED + "You're muted for another " + Util.getTimeUntil(tMute.getExpires()));
            }
            else {
                p.sendMessage(ChatColor.RED + "You're muted!");
            }
            event.setCancelled(true);
        }
    }
}
