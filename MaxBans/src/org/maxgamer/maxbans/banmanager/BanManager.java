package org.maxgamer.maxbans.banmanager;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.ChatColor;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;

public class BanManager{
	private MaxBans plugin;
	private HashMap<String, Ban> bans = new HashMap<String, Ban>();
	private HashMap<String, TempBan> tempbans = new HashMap<String, TempBan>();
	private HashMap<String, IPBan> ipbans = new HashMap<String, IPBan>();
	private HashMap<String, TempIPBan> tempipbans = new HashMap<String, TempIPBan>();
	private HashMap<String, Mute> mutes = new HashMap<String, Mute>();
	private HashMap<String, TempMute> tempmutes = new HashMap<String, TempMute>();
	
	//String: IP, HashSet: Lowercase usernames on that IP
	private HashMap<String, HashSet<String>> iphistory = new HashMap<String, HashSet<String>>();
	
	public boolean lockdown = false;
	public String lockdownReason = "None";
	
	private Database db;
	
	public BanManager(MaxBans plugin){
		this.plugin = plugin;
		this.db = plugin.getDB();
		
		this.reload();
	}
	
	
	/**
	 * Reloads from the database.
	 * Don't use this except when starting up.
	 */
	public void reload(){
		//TODO: Flush the dbwatcher.
		
		//Check the database is the same instance
		this.db = plugin.getDB();
		
		//Clear the memory cache
		this.bans.clear();
		this.tempbans.clear();
		this.ipbans.clear();
		this.tempipbans.clear();
		this.mutes.clear();
		this.tempmutes.clear();
		this.iphistory.clear();
		
		//Reload the cache from the database.
		String query = "none";
		plugin.getLogger().info("Loading from DB...");
		try{
			//Phase 1: Load bans
			plugin.getLogger().info("Loading bans");
			query = "SELECT * FROM bans";
			PreparedStatement ps = db.getConnection().prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String reason = rs.getString("reason");
				String banner = rs.getString("banner");
				
				long expires = rs.getLong("expires");
				long time = rs.getLong("time");
				
				if(expires != 0){
					TempBan tb = new TempBan(reason, banner, time, expires);
					this.tempbans.put(name, tb);
				}
				else{
					Ban ban = new Ban(reason, banner, time);
					this.bans.put(name, ban);
				}
			}
			
			//Phase 2: Load IP Bans
			plugin.getLogger().info("Loading ipbans");
			query = "SELECT * FROM ipbans";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String ip = rs.getString("ip");
				String reason = rs.getString("reason");
				String banner = rs.getString("banner");
				
				long expires = rs.getLong("expires");
				long time = rs.getLong("time");
				
				if(expires != 0){
					if(expires < System.currentTimeMillis()){
						db.getBuffer().addString("DELETE FROM ipbans WHERE ip = '"+ip+"' AND time <> 0");
					}
					else{
						TempIPBan tib = new TempIPBan(reason, banner, time, expires);
						this.tempipbans.put(ip, tib);
					}
				}
				else{
					IPBan ipban = new IPBan(reason, banner, time);
					this.ipbans.put(ip, ipban);
				}
			}
			
			//Phase 3: Load Mutes
			plugin.getLogger().info("Loading mutes");
			query = "SELECT * FROM mutes";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String banner = rs.getString("banner");
				
				long expires = rs.getLong("expires");
				long time = rs.getLong("time");
				
				if(expires != 0){
					if(expires < System.currentTimeMillis()){
						db.getBuffer().addString("DELETE FROM mutes WHERE name = '"+name+"' AND time <> 0");
					}
					else{
						TempMute tmute = new TempMute(banner, time, expires);
						this.tempmutes.put(name, tmute);
					}
				}
				else{
					Mute mute = new Mute(banner, time);
					this.mutes.put(name, mute);
				}
			}
			
			//Phase 4 loading: Load IP history
			plugin.getLogger().info("Loading IP History");
			query = "SELECT * FROM iphistory";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				
				HashSet<String> users = this.iphistory.get(ip);
				
				if(users == null){
					users = new HashSet<String>();
					this.iphistory.put(ip, users);
				}
				
				users.add(name);
			}
		}
		catch(SQLException e){
			plugin.getLogger().severe(ChatColor.RED + "Could not load database history using: " + query);
			e.printStackTrace();
		}
	}
    
	/**
	 * Fetches a mute on a player with a specific name
	 * @param name The name of the player.  Case doesn't matter.
	 * @return The mute object or null if they aren't muted.
	 * This will never return an expired mute.
	 */
    public Mute getMute(String name) {
    	name = name.toLowerCase();
    	
        Mute mute = mutes.get(name);
        if (mute !=null) {
            return mute;
        }
        TempMute tempm = tempmutes.get(name);
        if (tempm !=null) {
            if (System.currentTimeMillis() < tempm.getExpires()) {
                return tempm;
            }
            else{
            	tempmutes.remove(name);
            	String query = "DELETE FROM mutes WHERE Player = '"+name+"' AND expires <> 0";
            	db.getBuffer().addString(query);
            }
        }
        return null;
    }
    
    /**
     * Gets a ban by a players name
     * @param name The name of the player (any case)
     * @return The ban object or null if they are not banned.
     * Does not return expired bans, ever.
     */
    public Ban getBan(String name){
    	name = name.toLowerCase();
    	
    	Ban ban = bans.get(name);
    	if(ban != null){
    		return ban;
    	}
    	
    	TempBan tempBan = tempbans.get(name);
    	if(tempBan != null){
    		if(System.currentTimeMillis() < tempBan.getExpires()){
    			return tempBan;
    		}
    		else{
    			tempbans.remove(name);
    			String query = "DELETE FROM bans WHERE Player = '"+name+"' AND expires <> 0";
            	db.getBuffer().addString(query);
    		}
    	}
    	
    	return null;
    }
    
    /**
     * Fetches an IP ban from the database
     * @param ip The IP address to search for
     * @return The IPBan object, or null if there is no ban
     * Will never return an expired ban
     */
    public IPBan getIPBan(String ip){
    	IPBan ipBan = ipbans.get(ip);
    	if(ipBan != null){
    		return ipBan;
    	}
    	
    	TempIPBan tempIPBan = tempipbans.get(ip);
    	if(tempIPBan != null){
    		if(System.currentTimeMillis() < tempIPBan.getTime()){
            	return tempIPBan;
    		}
    		else{
    			ipbans.remove(ip);
    			String query = "DELETE FROM bans WHERE ip = '"+ip+"' AND expires <> 0";
            	db.getBuffer().addString(query);
    		}
    	}
    	return null;
    }
    
    /**
     * Fetches an IP ban from the database
     * @param ip The IP address to search for
     * @return The IPBan object, or null if there is no ban
     * Will never return an expired ban
     */
    public IPBan getIPBan(InetAddress addr){
    	return this.getIPBan(addr.getHostAddress());
    }
    
    /**
     * Creates a new ban and stores it in the database
     * @param name The name of the player who is banned
     * @param reason The reason they were banned
     * @param banner The admin who banned them
     */
    public void ban(String name, String reason, String banner){
    	banner = banner.toLowerCase();
    	Ban ban = new Ban(reason, banner, System.currentTimeMillis());
    	this.bans.put(name, ban);
    	plugin.getDB().getBuffer().addString("INSERT INTO bans (name, reason, banner, time) VALUES ('"+name+"','" + reason+"','" + banner+"','" + System.currentTimeMillis()+"');");
    }
    /**
     * @param name The name of the player.
     * @param reason Reason for the ban
     * @param banner The admin who banned them
     * @param expires Epoch time (Milliseconds) that they're unbanned.
     * Expirey time is NOT time from creating ban, it is milliseconds from 1970 (System.currentMillis())
     */
    public void tempban(String name, String reason, String banner, long expires){
    	name = name.toLowerCase();
    	banner = banner.toLowerCase();
    	
    	TempBan ban = new TempBan(reason, banner, System.currentTimeMillis(), expires);
    	this.tempbans.put(name, ban);
    	plugin.getDB().getBuffer().addString("INSERT INTO bans (name, reason, banner, time, expires) VALUES ('"+name+"','" + reason+"','" + banner+"','" + System.currentTimeMillis()+"','" + expires+"');");
    }
    
    /**
     * IP Bans an IP address so they can't join.
     * @param ip The IP to ban (e.g. 127.0.0.1)
     * @param reason The reason ("Misconduct!")
     * @param banner The admin who banned them
     */
    public void ipban(String ip, String reason, String banner){
    	banner = banner.toLowerCase();
    	
    	IPBan ipban = new IPBan(reason, banner, System.currentTimeMillis());
    	
    	this.ipbans.put(ip, ipban);
    	
    	plugin.getDB().getBuffer().addString("INSERT INTO ipbans (ip, reason, banner, time) VALUES ('"+ip+"','" + reason+"','" + banner+"','" + System.currentTimeMillis()+"');");
    }
    
    /**
     * IP Bans an IP address so they can't join.
     * @param ip The IP to ban (e.g. 127.0.0.1)
     * @param reason The reason ("Misconduct!")
     * @param banner The admin who banned them
     * @param expires The time the ban expires
     */
    public void tempipban(String ip, String reason, String banner, long expires){
    	banner = banner.toLowerCase();
    	
    	TempIPBan tib = new TempIPBan(reason, banner, System.currentTimeMillis(), expires);
    	
    	this.tempipbans.put(ip, tib);
    	
    	plugin.getDB().getBuffer().addString("INSERT INTO ipbans (ip, reason, banner, time, expires) VALUES ('"+ip+"','" + reason+"','" + banner+"','" + System.currentTimeMillis()+"','" + expires+"');");
    }
    
    /**
     * Mutes a player so they can't chat.
     * @param name The name of the player to mute
     * @param banner The admin who muted them
     */
    public void mute(String name, String banner){
    	name = name.toLowerCase();
    	banner = banner.toLowerCase();
    	
    	Mute mute = new Mute(banner, System.currentTimeMillis());
    	
    	this.mutes.put(name, mute);
    	
    	plugin.getDB().getBuffer().addString("INSERT INTO mutes (name, banner, time) VALUES ('"+name+"','" + banner+"','"+System.currentTimeMillis()+"');");
    }
    
    /**
     * Mutes a player so they can't chat.
     * @param name The name of the player to mute
     * @param banner The admin who muted them
     * @param expires The time the mute expires
     */
    public void tempmute(String name, String banner, long expires){
    	name = name.toLowerCase();
    	banner = banner.toLowerCase();
    	
    	TempMute tmute = new TempMute(banner, System.currentTimeMillis(), expires);
    	
    	this.tempmutes.put(name, tmute);
    	
    	plugin.getDB().getBuffer().addString("INSERT INTO mutes (name, banner, time, expires) VALUES ('"+name+"','" + banner+"','"+System.currentTimeMillis()+"','"+expires+"');");
    }
    
    /**
     * Fetches a hashset (unsorted list) of users whose most recent IP is the given IP address.
     * Will return null if no history for that IP address.
     * @param ip The IP to lookup
     * @return a hashset of users whose most recent IP is the given one
     */
    public HashSet<String> getUsersFromIP(String ip){
    	return this.iphistory.get(ip);
    }
    
    /**
     * Notes that a player joined from the given IP.
     * @param name Name of player. Case insensitive.
     * @param ip The ip they're connecting from.
     */
    public void logIP(String name, String ip){
    	name = name.toLowerCase();
    	
    	HashSet<String> users = this.getUsersFromIP(ip);
    	if(users == null){
    		users = new HashSet<String>();
    		this.iphistory.put(ip, users);
    	}
    	
    	//Hashsets eliminate duplicates anyway
    	users.add(name);
    	
    	this.db.getBuffer().addString("INSERT INTO iphistory (name, ip) VALUES ('"+name+"','"+ip+"')");
    }
    
    /**
     * Finds the time until a specific epoch.
     * @param epoch the epoch (Milliseconds) time to check
     * @return The time (String format) until the epoch ends in the format X weeks, Y days, Z hours, M minutes, S seconds. If values are 0 (X,Y,Z,M,S), it will ignore that segment. E.g. Mins = 0 so output will be [...] Z hours, S seconds [...]
     */
    public String getTimeUntil(long epoch){
    	epoch = epoch / 1000; //CBF dealing in milliseconds
    	StringBuilder sb = new StringBuilder(40);
    	
    	if(epoch % 604800 > 0){
    		//Days
    		sb.append(epoch % 604800 + " weeks, ");
    		epoch = epoch / 604800;
    	}
    	if(epoch % 86400 > 0){
    		//Days
    		sb.append(epoch % 86400 + " days, ");
    		epoch = epoch / 86400;
    	}
    	
    	if(epoch % 3600 > 0){
    		//More than one hour
    		
    		sb.append((epoch % 3600) + " hours,");
    		epoch = epoch / 3600;
    	}
    	
    	if(epoch % 60 > 0){
    		sb.append((epoch % 60) + " minutes,");
    		epoch = epoch / 60;
    	}
    	
    	if(epoch > 0){
    		sb.append((epoch) + " seconds.");
    	}
    	
    	if(sb.charAt(sb.length() - 1) == ','){
    		sb.replace(sb.length(), sb.length(), ",");
    	}
    	return sb.toString();
    }
}
