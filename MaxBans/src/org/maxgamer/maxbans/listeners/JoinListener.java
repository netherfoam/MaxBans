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
            
            //Ban
            Ban ban = plugin.getBanManager().getBan(address.getHostAddress());
            
            //IP Ban
            IPBan ipban= plugin.getBanManager().getIPBan(player.getName());
            
            
            if (ipban == null && ban == null) {
            	return;
            } 
            else {
                String reason;
                String banner;
                long timeLifted=0;
                if (ipban!=null){ 
                    //don't really want to split up IPBan and Ban but can't find a good way to connect them, use any ideas you have if you want
                    if (ipban instanceof TempIPBan) {
                    	TempIPBan tempipban = (TempIPBan) ipban;
                        timeLifted = tempipban.getTimeOfUnban(); //wish there was a better way to do this
                    }
                    reason = ipban.getReason();
                    banner = ipban.getBanner();
                } else{
                    if (ban instanceof TempBan) {
                    	TempBan tempban = (TempBan) ban;
                        timeLifted = tempban.getTimeOfUnban();
                    }
                    reason = ban.getReason();
                    banner = ban.getBanner();
                }
                
                StringBuilder km = new StringBuilder(25); //kickmessage
                km.append("You\'re banned!\n Reason: ");
                km.append(reason);
                km.append("\n By ");
                km.append(banner);  //this is probably going to make the length too long
                					// It's more info, it shouldnt be an issue. We can use \n now too!
                					// ThankYou patch notes!
                if (timeLifted >0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
                    km.append(". Expires on: ");
                    km.append(sdf.format(timeLifted));
                    km.append(" server time."); //we'll add timezone support later
                    							//Suggestion => Wouldn't it be easier
                    							// To tell them how long til their
                    							//Ban is lifted instead? E.g. 3 days 2 hours
                    							//Is easier than GEO IP'ing them and everything
                }
                event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                event.setKickMessage(km.toString());
            }
            
            
            
    }
}
