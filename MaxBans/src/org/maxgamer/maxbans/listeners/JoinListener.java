package org.maxgamer.maxbans.listeners;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.commands.DupeIPCommand;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.RangeBan;
import org.maxgamer.maxbans.util.Util;

public class JoinListener extends ListenerSkeleton{
	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoinDupeip(PlayerLoginEvent e){
		if(plugin.getConfig().getBoolean("auto-dupeip") == false || e.getResult() != Result.ALLOWED){
			return;
		}
		
		HashSet<String> dupes = plugin.getBanManager().getUsers(e.getAddress().getHostAddress());
		if(dupes == null){
			return;
		}
		dupes.remove(e.getPlayer().getName().toLowerCase());
		if(dupes.isEmpty()){
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		for(String dupe : dupes){
			sb.append(DupeIPCommand.getChatColor(dupe).toString()+ dupe + ", ");
		}
		
		sb.replace(sb.length() - 2, sb.length(), "");
		for(Player p : Bukkit.getOnlinePlayers()){
			if(p.hasPermission("maxbans.notify")){
				p.sendMessage(DupeIPCommand.getScanningString(e.getPlayer().getName().toLowerCase(), e.getAddress().getHostAddress()));
				p.sendMessage(sb.toString());
			}
		}
		
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
							p.sendMessage(ChatColor.RED + "Bypassing lockdown (" + plugin.getBanManager().getLockdownReason() + ")!");
						}
					}
	        		
	        	}, 40);
	        }
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoinHandler(PlayerLoginEvent e) {
    	if(e.getResult() != Result.ALLOWED){
    		return;
    	}
    	
        final Player player = e.getPlayer();
        final String address = e.getAddress().getHostAddress();
        
        if(plugin.filter_names){
	        String invalidChars = Util.getInvalidChars(player.getName());
	        if(!invalidChars.isEmpty()){
	        	e.setKickMessage("Kicked by MaxBans.\nYour name contains invalid characters:\n'" + invalidChars + "'");
	        	e.setResult(Result.KICK_OTHER);
	        	return;
	        }
	        else if(player.getName().isEmpty()){
	        	e.setKickMessage("Kicked by MaxBans.\nYour name is invalid!");
	        	e.setResult(Result.KICK_OTHER);
	        	return;
	        }
        }
        
        //Log that the player connected from that IP address.
        if(plugin.getBanManager().logIP(player.getName(), address)){
        	if(plugin.getSyncer() != null){
	    		Packet ipUpdate = new Packet().setCommand("setip").put("name", player.getName());
	    		ipUpdate.put("ip", address);
	    		plugin.getSyncer().broadcast(ipUpdate);
	    	}
        }
        
        //Log the players actual case-sensitive name.
        if(plugin.getBanManager().logActual(player.getName(), player.getName())){
        	if(plugin.getSyncer() != null){
	    		Packet nameUpdate = new Packet().setCommand("setname").put("name", player.getName());
	    		plugin.getSyncer().broadcast(nameUpdate);
	    	}
        }
        
        //Ban
        Ban ban = plugin.getBanManager().getBan(player.getName());
        
        //IP Ban
        IPBan ipban = null;
        boolean whitelisted = plugin.getBanManager().isWhitelisted(player.getName());
        if(!whitelisted){ //Only fetch the IP ban if the user is not whitelisted.
        	 ipban = plugin.getBanManager().getIPBan(address); 
        }
        
        //If they haven't been banned or IP banned, they can join.
        if(ipban == null && ban == null){
        	if(!whitelisted){
        		//Check for a rangeban
	        	IPAddress ip = new IPAddress(address);
	        	RangeBan rb = plugin.getBanManager().getRanger().getBan(ip);
	        	if(rb != null){
	                e.disallow(Result.KICK_OTHER, rb.getKickMessage());
	                
	                if(plugin.getConfig().getBoolean("notify", true)){
	                	String msg = Formatter.secondary + player.getName() + Formatter.primary + " (" + ChatColor.RED + address + Formatter.primary + ")" + " tried to join, but is " + (rb instanceof Temporary ? "temp " : "") + "RangeBanned.";
	        	        for(Player p : Bukkit.getOnlinePlayers()){
	        	        	if(p.hasPermission("maxbans.notify")){
	        	        		p.sendMessage(msg);
	        	        	}
	        	        }
	                }
	                
	                return;
	        	}
	        	
	        	//DNS Blacklist handling, only if NOT whitelisted
	            if(plugin.getBanManager().getDNSBL() != null){
	            	plugin.getBanManager().getDNSBL().handle(e);
	            	if(e.getResult() != Result.ALLOWED) return; //DNSBL doesn't want them joining.
	            }
        	}
        	
        	return;
        }
        
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage((ipban != null ? ipban.getKickMessage() : ban.getKickMessage()));
        
        if(plugin.getConfig().getBoolean("notify", true)){
        	String msg = (ban == null ? Formatter.secondary : ChatColor.RED) + player.getName() + Formatter.primary + " (" + (ipban == null ? Formatter.secondary : ChatColor.RED) + address + Formatter.primary + ") tried to join, but is "+ ((ban instanceof Temporary || ipban instanceof Temporary) ? "temp " : "") +"banned!"; 
	        for(Player p : Bukkit.getOnlinePlayers()){
	        	if(p.hasPermission("maxbans.notify")){
	        		p.sendMessage(msg);
	        	}
	        }
        }
    }
}
