package org.maxgamer.maxbans.listeners;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.*;

public class JoinListener implements Listener{
        private MaxBans plugin;
        public JoinListener(MaxBans mb){
            plugin=mb;
        }
        @EventHandler (priority = EventPriority.LOWEST)
        public void onJoinHandler(PlayerLoginEvent event) {
            Player player = event.getPlayer();
            InetAddress address = event.getAddress();
            Ban ban= null;
            IPBan ipban=null;
            if ((ipban=plugin.getBanManager().isIPBanned(address.getHostAddress())) ==null && //IPBan takes priority
                    (ban=plugin.getBanManager().isBanned(player.getName())) ==null) {
            } else {
                String reason=null;
                String banner = null;
                long timeLifted=0;
                if (ipban!=null){ 
                    //don't really want to split up IPBan and Ban but can't find a good way to connect them, use any ideas you have if you want
                    TempIPBan tempipban = (TempIPBan) ipban;
                    if (tempipban !=null) {
                        timeLifted = tempipban.getTimeOfUnban(); //wish there was a better way to do this
                    }
                    reason = ipban.getReason();
                    banner = ipban.getBanner();
                } else if (ban!=null){
                    TempBan tempban = (TempBan) ban;
                    if (tempban !=null) {
                        timeLifted = tempban.getTimeOfUnban();
                    }
                    reason = ban.getReason();
                    banner = ban.getBanner();
                } else {
                    plugin.getLogger().severe("Something went wrong with JoinHandler");
                    return;
                }
                StringBuilder km = new StringBuilder(7); //kickmessage
                km.append("You are banned! Reason: ");
                km.append(reason);
//                km.append(" - ");
//                km.append(banner); this is probably going to make the length too long
                if (timeLifted >0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    km.append(";Expires on: ");
                    km.append(sdf.format(timeLifted));
                    km.append(" server time."); //we'll add timezone support later
                }
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                event.setKickMessage(km.toString());
            }
            
            
            
    }
}
