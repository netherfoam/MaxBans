package org.maxgamer.maxbans.listeners;

import java.net.InetAddress;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.*;

public class JoinListener implements Listener{
    private MaxBans plugin;
    public JoinListener(MaxBans plugin){
        this.plugin = plugin;
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoinHandler(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        if(this.plugin.getBanManager().lockdown && !player.hasPermission("maxbans.bypass.lockdown")){
    		event.setKickMessage(plugin.getBanManager().lockdownReason);
    		event.setResult(Result.KICK_OTHER);
    		return;
    	}
        
        InetAddress address = event.getAddress();
        
        //Ban
        Ban ban = plugin.getBanManager().getBan(player.getName());
        
        //IP Ban
        IPBan ipban= plugin.getBanManager().getIPBan(address);
        
        //If they havent been banned or IP banned, they can join.
        if(ipban == null && ban == null){
        	return;
        }
        
        String reason;
        String banner;
        long expires = 0;
        
        if (ipban != null){ 
            if (ipban instanceof TempIPBan) {
            	TempIPBan tempipban = (TempIPBan) ipban;
            	expires = tempipban.getExpires(); //wish there was a better way to do this
            }
            reason = ipban.getReason();
            banner = ipban.getBanner();
            
        } else{ //We dont need to check ban isn't null here. We already did.
            if (ban instanceof TempBan) {
            	TempBan tempban = (TempBan) ban;
            	expires = tempban.getExpires();
            }
            reason = ban.getReason();
            banner = ban.getBanner();
        }
        
        StringBuilder km = new StringBuilder(25); //kickmessage
        km.append("You\'re banned!\n Reason: ");
        km.append(reason);
        km.append("\n By ");
        km.append(banner + ". ");  //this is probably going to make the length too long
        					// It's more info, it shouldnt be an issue. We can use \n now too!
        					// ThankYou patch notes!
        if (expires > 0) {
        	km.append("Expires in " + plugin.getBanManager().getTimeUntil(expires));
        	/*
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");
            km.append(". Expires on: ");
            km.append(sdf.format(expires));
            km.append(" server time."); //we'll add timezone support later
            							//Suggestion => Wouldn't it be easier
            							// To tell them how long til their
            							// Ban is lifted instead? E.g. 3 days 2 hours
            							// Is easier than GEO IP'ing them and everything*/
        }
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        event.setKickMessage(km.toString());
    }
}
