package org.maxgamer.maxbans.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.*;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Util;

public class JoinListener implements Listener{
    private MaxBans plugin;
    
    public JoinListener(MaxBans plugin){
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOW)
	public void onJoinLockdown(PlayerLoginEvent event) {
		if(event.getResult() != Result.ALLOWED) return;
    	Player player = event.getPlayer();
    	if(plugin.getBanManager().isLockdown()){
	        if(!player.hasPermission("maxbans.lockdown.bypass")){
	    		event.setKickMessage("Server is in lockdown mode. Try again shortly. Reason: \n" + plugin.getBanManager().getLockdownReason());
	    		event.setResult(Result.KICK_OTHER);
	    		return;
	    	}
	        else{ //Delay this, because it's fucken more important than essentials
	        	final String name = player.getName();
	        	Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, new Runnable(){
					public void run() {
						Player p = Bukkit.getPlayerExact(name);
						if(p != null){
							p.sendMessage(ChatColor.RED + "Bypassing lockdown!");
						}
					}
	        		
	        	}, 40);
	        }
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoinHandler(PlayerLoginEvent event) {
        final Player player = event.getPlayer();
        final String address = event.getAddress().getHostAddress();
        
        if(plugin.filter_names){
	        String invalidChars = Util.getInvalidChars(player.getName());
	        if(!invalidChars.isEmpty()){
	        	event.setKickMessage("Kicked by MaxBans.\nYour name contains invalid characters:\n'" + invalidChars + "'");
	        	event.setResult(Result.KICK_OTHER);
	        	return;
	        }
	        else if(player.getName().isEmpty()){
	        	event.setKickMessage("Kicked by MaxBans.\nYour name is invalid!");
	        	event.setResult(Result.KICK_OTHER);
	        	return;
	        }
        }
        
        //Ban
        Ban ban = plugin.getBanManager().getBan(player.getName());
        
        //IP Ban
        IPBan ipban= plugin.getBanManager().getIPBan(address);
        
        //If they haven't been banned or IP banned, they can join.
        if(ipban == null && ban == null){        	
        	//DNS Blacklist handling.
            if(plugin.getBanManager().getDNSBL() != null){
            	plugin.getBanManager().getDNSBL().handle(event);
            	if(event.getResult() != Result.ALLOWED) return; //DNSBL doesn't want them joining.
            }
        	
        	plugin.getBanManager().logIP(player.getName(), address);
        	
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
        km.append(Formatter.message + "You're "+(ipban == null ? "" : "IP ")+"banned!" + Formatter.regular + "\n Reason: ");
        km.append(Formatter.reason + reason);
        km.append(Formatter.regular + "\n By ");
        km.append(Formatter.banner + banner + Formatter.regular + ". ");
        if (expires > 0) {
        	km.append("Expires in " + Formatter.time + Util.getTimeUntil(expires));
        }
        String appeal = plugin.getConfig().getString("appeal-message");
        if(appeal != null && !appeal.isEmpty()) km.append("\n" + appeal);
        
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        event.setKickMessage(km.toString());
        
        if(plugin.getConfig().getBoolean("notify", true)){
        	String msg = (ban == null ? Formatter.secondary : ChatColor.RED) + player.getName() + Formatter.primary + " (" + (ipban == null ? Formatter.secondary : ChatColor.RED) + address + Formatter.primary + ") tried to join, but is "+ (expires > 0 ? "temp banned" : "banned") +"!"; 
	        for(Player p : Bukkit.getOnlinePlayers()){
	        	if(p.hasPermission("maxbans.notify")){
	        		p.sendMessage(msg);
	        	}
	        }
        }
    }
}
