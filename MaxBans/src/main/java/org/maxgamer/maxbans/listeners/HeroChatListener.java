package org.maxgamer.maxbans.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.maxgamer.maxbans.banmanager.Mute;
import org.bukkit.entity.Player;
import com.dthielke.herochat.Chatter;
import org.maxgamer.maxbans.util.Util;
import org.bukkit.ChatColor;
import org.maxgamer.maxbans.banmanager.TempMute;
import com.dthielke.herochat.ChannelChatEvent;
import org.maxgamer.maxbans.MaxBans;
import org.bukkit.event.Listener;

public class HeroChatListener implements Listener
{
    private MaxBans plugin;
    
    public HeroChatListener(final MaxBans plugin) {
        super();
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onHeroChat(final ChannelChatEvent e) {
        final Player p = e.getSender().getPlayer();
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
            e.setResult(Chatter.Result.FAIL);
        }
    }
}
