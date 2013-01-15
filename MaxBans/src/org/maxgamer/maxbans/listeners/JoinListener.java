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
import org.maxgamer.maxbans.util.DNSBL;
import org.maxgamer.maxbans.util.DNSBL.CacheRecord;
import org.maxgamer.maxbans.util.DNSBL.DNSStatus;
import org.maxgamer.maxbans.util.Util;

public class JoinListener implements Listener{
    private MaxBans plugin;
    
    public JoinListener(MaxBans plugin){
        this.plugin = plugin;
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
        
        //If they havent been banned or IP banned, they can join.
        if(ipban == null && ban == null){
        	
        	//DNS Blacklist handling.
            if(plugin.getBanManager().getDNSBL() != null){
            	final DNSBL dnsbl = plugin.getBanManager().getDNSBL();
            	CacheRecord r = dnsbl.getRecord(address);
            	
            	//We have no record of their IP, or it has expired.
            	if(r == null){
            		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
						public void run() {
							//Fetch the new status, this method also caches it for future use.
							DNSStatus status = plugin.getBanManager().getDNSBL().reload(address);
							if(status == DNSStatus.DENIED){
								//Notify console.
								Bukkit.getScheduler().runTask(plugin, new Runnable(){
									public void run(){
										if(dnsbl.kick && player.isOnline()){
											player.kickPlayer("Kicked by MaxBans:\nYour IP ("+address+") is listed as a proxy.");
										}
										if(dnsbl.notify){ 
											String msg = plugin.color_secondary + player.getName() + plugin.color_primary + " (" + plugin.color_secondary + address + plugin.color_primary + ") is joining from a proxy IP!";
											for(Player p : Bukkit.getOnlinePlayers()){
												if(p.hasPermission("maxbans.notify")){
													p.sendMessage(msg);
												}
											}
										}
										Bukkit.getLogger().info(player.getName() + " is using a proxy IP!");
									}
								});
							}
						}
            		});
            	}
            	else if(r.getStatus() == DNSStatus.DENIED){
            		if(dnsbl.notify){
            			String msg = plugin.color_secondary + player.getName() + plugin.color_primary + " (" + plugin.color_secondary + address + plugin.color_primary + ") is joining from a proxy IP!";
						for(Player p : Bukkit.getOnlinePlayers()){
							if(p.hasPermission("maxbans.notify")){
								p.sendMessage(msg);
							}
						}
            		}
        			Bukkit.getLogger().info(player.getName() + " is using a proxy IP!");
            		if(dnsbl.kick){
            			event.disallow(Result.KICK_OTHER, "Kicked By MaxBans:\nYour IP ("+address+") is listed as a proxy.");
            			return; 
            		}
            	}
            }
        	
        	plugin.getBanManager().logIP(player.getName(), address);
        	
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
        km.append("You\'re "+(ipban == null ? "" : "IP ")+"banned!\n Reason: ");
        km.append(reason);
        km.append("\n By ");
        km.append(banner + ". ");  //this is probably going to make the length too long
        					// It's more info, it shouldnt be an issue. We can use \n now too!
        					// ThankYou patch notes!
        if (expires > 0) {
        	km.append("Expires in " + Util.getTimeUntil(expires));
        }
        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        event.setKickMessage(km.toString());
        
        if(plugin.getConfig().getBoolean("notify", true)){
        	String msg = (ban == null ? plugin.color_secondary : ChatColor.RED) + player.getName() + plugin.color_primary + " (" + (ipban == null ? plugin.color_secondary : ChatColor.RED) + address + plugin.color_primary + ") tried to join, but is "+ (expires > 0 ? "temp banned" : "banned") +"!"; 
	        for(Player p : Bukkit.getOnlinePlayers()){
	        	if(p.hasPermission("maxbans.notify")){
	        		p.sendMessage(msg);
	        	}
	        }
        }
    }
}
