package org.maxgamer.maxbans.listeners;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Ban;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.commands.DupeIPCommand;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

public class JoinListener extends ListenerSkeleton{
	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoinDupeip(final PlayerLoginEvent e){
		if(this.getPlugin().getConfig().getBoolean("auto-dupeip") == false || e.getResult() != Result.ALLOWED){
			return;
		}
		
		Runnable r = new Runnable(){
			@Override
			public void run(){
				HashSet<String> dupes = getPlugin().getBanManager().getUsers(e.getAddress().getHostAddress());
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
		};
		
		if(getPlugin().isBungee()){
			//If it's Bungee, we need to wait for their IP to update correctly in the next tick.
			Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), r);
		}
		else{
			//Else, we can just do it now.
			r.run();
		}
	}
	
    @EventHandler(priority = EventPriority.LOW)
	public void onJoinLockdown(PlayerLoginEvent event) {
    	Player player = event.getPlayer();
    	if(this.getPlugin().getBanManager().isLockdown()){
	        if(!player.hasPermission("maxbans.lockdown.bypass")){
	    		event.setKickMessage("Server is in lockdown mode. Try again shortly. Reason: \n" + this.getPlugin().getBanManager().getLockdownReason());
	    		event.setResult(Result.KICK_OTHER);
	    		return;
	    	}
	        else{ //Delay this, because it's fucken more important than essentials
	        	final String name = player.getName();
	        	Bukkit.getScheduler().runTaskLaterAsynchronously(this.getPlugin(), new Runnable(){
					public void run() {
						Player p = Bukkit.getPlayerExact(name);
						if(p != null){
							p.sendMessage(ChatColor.RED + "Bypassing lockdown (" + getPlugin().getBanManager().getLockdownReason() + ")!");
						}
					}
	        		
	        	}, 40);
	        }
        }
    }
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onJoinHandler(final PlayerLoginEvent e) {    	
        final Player player = e.getPlayer();
        
        if(this.getPlugin().getBanManager().hasImmunity(player.getName())){
    		return; 
    	}
        
        if(this.getPlugin().filter_names){
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
        
        if(MaxBans.instance.isBungee()){
        	//Bungee things
        	
        	//This has to be executed in the next tick, because the player isn't 'online' yet. I think.
        	Bukkit.getScheduler().scheduleSyncDelayedTask(MaxBans.instance, new Runnable(){
        		@Override
        		public void run(){
        			//Ask Bungee for the players real IP
            		ByteArrayOutputStream b = new ByteArrayOutputStream();
            		DataOutputStream out = new DataOutputStream(b);
            		try {
            			out.writeUTF("IP");
        			} catch (IOException e1) {} //Can't happen
            		
            		e.getPlayer().sendPluginMessage(MaxBans.instance, MaxBans.BUNGEE_CHANNEL, b.toByteArray());
            		//The BungeeListener handles the response.
        		}
        	});
    	}
    	else{
    		final String address = e.getAddress().getHostAddress();
    		//Specifically NON-BUNGEE things.
    		//Log that the player connected from that IP address.
            this.getPlugin().getBanManager().logIP(player.getName(), address);
            
            //Whitelisted players may bypass IP restrictions.
            boolean whitelisted = this.getPlugin().getBanManager().isWhitelisted(player.getName());
            
            if(!whitelisted){
            	IPBan ipban = this.getPlugin().getBanManager().getIPBan(address); 
            	if(ipban != null){
            		e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                    e.setKickMessage(ipban.getKickMessage());
                    return; //Failure - IPBanned
            	}
            	
                //Check for a rangeban
	        	IPAddress ip = new IPAddress(address);
	        	RangeBan rb = this.getPlugin().getBanManager().getBan(ip);
	        	if(rb != null){
	                e.disallow(Result.KICK_OTHER, rb.getKickMessage());
	                
	                if(this.getPlugin().getConfig().getBoolean("notify", true)){
	                	String msg = Formatter.secondary + player.getName() + Formatter.primary + " (" + ChatColor.RED + address + Formatter.primary + ")" + " tried to join, but is " + (rb instanceof Temporary ? "temp " : "") + "RangeBanned.";
	        	        for(Player p : Bukkit.getOnlinePlayers()){
	        	        	if(p.hasPermission("maxbans.notify")){
	        	        		p.sendMessage(msg);
	        	        	}
	        	        }
	                }
	                
	                return; //Failure - Rangebanned
	        	}
	        	
	        	//DNS Blacklist handling, only if NOT whitelisted
	            if(this.getPlugin().getBanManager().getDNSBL() != null){
	            	this.getPlugin().getBanManager().getDNSBL().handle(e);
	            }
        	}
    	}
        
        
        //Log the players actual case-sensitive name.
        if(this.getPlugin().getBanManager().logActual(player.getName(), player.getName())){
        	if(this.getPlugin().getSyncer() != null){
	    		Packet nameUpdate = new Packet().setCommand("setname").put("name", player.getName());
	    		this.getPlugin().getSyncer().broadcast(nameUpdate);
	    	}
        }
        
        //Ban
        Ban ban = this.getPlugin().getBanManager().getBan(player.getName());
        
        //If they haven't been banned or IP banned, they can join.
        if(ban == null){
        	return;
        }
        
        e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        e.setKickMessage(ban.getKickMessage());
        
        if(this.getPlugin().getConfig().getBoolean("notify", true)){ 
        	String msg = (ban == null ? Formatter.secondary : ChatColor.RED) + player.getName() + Formatter.primary + " tried to join, but is "+ ((ban instanceof Temporary) ? "temp " : "") +"banned!";
	        for(Player p : Bukkit.getOnlinePlayers()){
	        	if(p.hasPermission("maxbans.notify")){
	        		p.sendMessage(msg);
	        	}
	        }
        }
    }
}
