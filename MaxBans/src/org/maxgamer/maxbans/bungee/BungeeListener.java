package org.maxgamer.maxbans.bungee;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.IPBan;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.Temporary;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.IPAddress;

public class BungeeListener implements PluginMessageListener{
	private MaxBans plugin = MaxBans.instance;
	@Override
	public void onPluginMessageReceived(String channel, final Player player, final byte[] message) {
		try{
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
			if(in.readUTF().equals("IP")){
				final String ip = in.readUTF();
				MaxBans.instance.getBanManager().logIP(player.getName(), ip);
				
				//We should wait another tick to stop the user from being disconnected with 'end of stream' instead.
				Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
					@Override
					public void run(){
						//IP Ban
				        boolean whitelisted = MaxBans.instance.getBanManager().isWhitelisted(player.getName());
				        if(!whitelisted){ //Only fetch the IP ban if the user is not whitelisted.
				        	IPBan ipban = plugin.getBanManager().getIPBan(ip); 
			            	if(ipban != null){
			            		player.kickPlayer(ipban.getKickMessage());
			                    return; //Failure - IPBanned
			            	}
			            	
			                //Check for a rangeban
				        	IPAddress address = new IPAddress(ip);
				        	RangeBan rb = plugin.getBanManager().getBan(address);
				        	if(rb != null){
				        		player.kickPlayer(rb.getKickMessage());
				                
				                if(plugin.getConfig().getBoolean("notify", true)){
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
				            if(plugin.getBanManager().getDNSBL() != null){
				            	//plugin.getBanManager().getDNSBL().handle(e);
				            	plugin.getBanManager().getDNSBL().handle(player, ip);
				            }
				        }
					}
				}, 1);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}