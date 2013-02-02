package org.maxgamer.maxbans.banmanager;

import java.net.InetAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.util.DNSBL;
import org.maxgamer.maxbans.util.Formatter;

/**
 * The ban manager class.
 * <br/>
 * <br/>
 * This BanManager provides an API to ban, ipban,
 * warn, mute, lockdown, and lookup IP history.
 * All of its methods save to the database, so there
 * is no need to write information to the database
 * yourself.
 * <br/>
 * <br/>
 * 
 * @author netherfoam
 */
public class BanManager{
	private MaxBans plugin;
	private HashMap<String, Ban> bans = new HashMap<String, Ban>();
	private HashMap<String, TempBan> tempbans = new HashMap<String, TempBan>();
	private HashMap<String, IPBan> ipbans = new HashMap<String, IPBan>();
	private HashMap<String, TempIPBan> tempipbans = new HashMap<String, TempIPBan>();
	private HashMap<String, Mute> mutes = new HashMap<String, Mute>();
	private HashMap<String, TempMute> tempmutes = new HashMap<String, TempMute>();
	private HashMap<String, List<Warn>> warnings = new HashMap<String, List<Warn>>();
	/** The recent actions that have occured */
	private ArrayList<HistoryRecord> history = new ArrayList<HistoryRecord>(50);
	
	/** Hashmap of Usernamep, IP address */
	private HashMap<String, String> recentips = new HashMap<String, String>();
	/** Hashmap of IP Address, users from that IP address */
	private HashMap<String, HashSet<String>> iplookup = new HashMap<String, HashSet<String>>(); 
	/** Player names */
	private TrieSet players = new TrieSet();
	/** Commands that send chat messages - Such as /me, /action and /say */
	private HashSet<String> chatCommands = new HashSet<String>();
	
	/** Whether the server is in lockdown mode or not */
	private boolean lockdown = false;
	/** The resaon the server is in lockdown - Defaults to Maintenance */
	private String lockdownReason = "Maintenance";
	
	/** Default reason for punishments */
	public String defaultReason = "Misconduct";
	
	/** Appeal message appended when a player is prevented from joining. */
	private String appealMessage = "";
	
	/** The database that we should use */
	private Database db;
	
	/** The DNS Blacklist */
	private DNSBL dnsbl; 
	
	public BanManager(MaxBans plugin){
		this.plugin = plugin;
		this.db = plugin.getDB();
		
		this.reload();
	}
	
	/** Returns the appeal message */
	public String getAppealMessage(){ return appealMessage; }
	/** Sets the appeal message. Translates & to ChatColor's */
	public void setAppealMessage(String msg){ this.appealMessage = ChatColor.translateAlternateColorCodes('&', msg); }
	
	/** Returns a hashmap of bans.  Do not edit these. */
	public HashMap<String, Ban> getBans(){ return bans; }
	/** Returns a hashmap of ip bans. Do not edit these. */
	public HashMap<String, IPBan> getIPBans(){ return ipbans; }
	/** Returns a hashmap of mutes. Do not edit these. */
	public HashMap<String, Mute> getMutes(){ return mutes; }
	/** The things that have happened recently. getHistory()[0] is the most recent thing that happened. */
	public HistoryRecord[] getHistory(){ return history.toArray(new HistoryRecord[history.size()]); }
	/**
	 * Adds the given string as a history message.
	 * @param s The string to add.
	 * This method adds the message to the database records.
	 */
	public void addHistory(String s){
		history.add(0, new HistoryRecord(s));
		plugin.getDB().execute("INSERT INTO history (created, message) VALUES (?, ?)", System.currentTimeMillis(), s);
	}
	
	/**
	 * Reloads from the database.
	 * Don't use this except when starting up.  It is very resource intensive.
	 */
	public void reload(){
		//Check the database is the same instance
		this.db = plugin.getDB();
		
		if(db.getTask() != null){
			db.getTask().cancel();
		}
		if(db.getDatabaseWatcher() != null){
			db.getDatabaseWatcher().run(); //Clears it. Does not restart it.
		}
		
		
		//Clear the memory cache
		this.bans.clear();
		this.tempbans.clear();
		this.ipbans.clear();
		this.tempipbans.clear();
		this.mutes.clear();
		this.tempmutes.clear();
		this.recentips.clear();
		this.players.clear();
		
		plugin.reloadConfig();
		
		this.lockdown = plugin.getConfig().getBoolean("lockdown");
		this.lockdownReason = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("lockdown-reason", ""));
		setAppealMessage(plugin.getConfig().getString("appeal-message", "")); //Default to empty string.
		
		//Reload the cache from the database.
		String query = "";
		plugin.getLogger().info("Loading from DB...");
		try{
			//Close any old connections (Possibly fix SQLITE_BUSY on reload?)
			db.getConnection().close();
			
			//Phase 1: Load bans
			
			//Purge old temp bans
			PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM bans WHERE expires <> 0 AND expires < ?");
			ps.setLong(1, System.currentTimeMillis());
			ps.execute(); 
			
			plugin.getLogger().info("Loading bans");
			query = "SELECT * FROM bans";
			ps = db.getConnection().prepareStatement(query);
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
			
			//Purge old temp ip bans
			ps = db.getConnection().prepareStatement("DELETE FROM ipbans WHERE expires <> 0 AND expires < ?");
			ps.setLong(1, System.currentTimeMillis());
			ps.execute(); 
			
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
					TempIPBan tib = new TempIPBan(reason, banner, time, expires);
					this.tempipbans.put(ip, tib);
				}
				else{
					IPBan ipban = new IPBan(reason, banner, time);
					this.ipbans.put(ip, ipban);
				}
			}
			
			//Phase 3: Load Mutes
			
			//Purge old temp mutes
			ps = db.getConnection().prepareStatement("DELETE FROM mutes WHERE expires <> 0 AND expires < ?");
			ps.setLong(1, System.currentTimeMillis());
			ps.execute(); 
			
			plugin.getLogger().info("Loading mutes");
			query = "SELECT * FROM mutes";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String banner = rs.getString("muter");
				
				long expires = rs.getLong("expires");
				long time = rs.getLong("time");
				
				if(expires != 0){
					TempMute tmute = new TempMute(banner, time, expires);
					this.tempmutes.put(name, tmute);
				}
				else{
					Mute mute = new Mute(banner, time);
					this.mutes.put(name, mute);
				}
			}
			
			//Phase 4 loading: Load IP history & Player history
			plugin.getLogger().info("Loading IP History");
			query = "SELECT * FROM iphistory";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String ip = rs.getString("ip");
				
				this.recentips.put(name, ip);
				HashSet<String> list = this.iplookup.get(ip);
				if(list == null){
					list = new HashSet<String>(8);
					this.iplookup.put(ip, list);
				}
				list.add(name);
				
				this.players.add(name);
			}
			
			//Phase 5 loading: Load Warn history
			
			//Purge old warnings
			ps = db.getConnection().prepareStatement("DELETE FROM warnings WHERE expires < ?");
			ps.setLong(1, System.currentTimeMillis());
			ps.execute(); 
			
			plugin.getLogger().info("Loading warn history...");
			//We only want warns that haven't expired.
			query = "SELECT * FROM warnings";
			ps = db.getConnection().prepareStatement(query);
			rs = ps.executeQuery();
			
			while(rs.next()){
				String name = rs.getString("name");
				String reason = rs.getString("reason");
				String banner = rs.getString("banner");
				long expires = rs.getLong("expires");
				
				Warn warn = new Warn(reason,banner, expires);
				
				List<Warn> warns = this.warnings.get(name);
				if(warns == null){
					warns = new ArrayList<Warn>();
					this.warnings.put(name, warns);
				}
				warns.add(warn);
			}
			
			//Phase 6 loading: Load Chat Commands
			plugin.getLogger().info("Loading chat commands...");
			List<String> cmds = plugin.getConfig().getStringList("chat-commands");
			for(String s : cmds){
				this.addChatCommand(s);
			}
			
			//Phase 7 loading: Load history
			plugin.getLogger().info("Loading history...");
			
			db.getConnection().prepareStatement("DELETE FROM history WHERE created < " + (System.currentTimeMillis() - plugin.getConfig().getInt("history-expirey-minutes", 10080) * 60000)).execute();
			rs = db.getConnection().prepareStatement("SELECT * FROM history ORDER BY created DESC").executeQuery();
			while(rs.next()){
				this.history.add(new HistoryRecord(rs.getString("message"), rs.getLong("created")));
			}
			rs.close();
		}
		catch(SQLException e){
			plugin.getLogger().severe(Formatter.secondary + "Could not load database history using: " + query);
			e.printStackTrace();
		}
		
		if(plugin.getConfig().getBoolean("dnsbl.use", true)){
			plugin.getLogger().info("Starting DNS blacklist");
			this.dnsbl = new DNSBL(plugin);
		}
		
		//Load the default ban reason - Default to Misconduct if none given
		String defaultReason = plugin.getConfig().getString("default-reason");
		if(defaultReason == null || defaultReason.isEmpty()) defaultReason = "Misconduct";
		this.defaultReason = ChatColor.translateAlternateColorCodes('&', defaultReason);

		db.scheduleWatcher(); //Actually starts it.
	}
	
	/**
	 * The DNS Blacklist object, or NULL if it is disabled.
	 * @return The DNS Blacklist object, or NULL if it is disabled.
	 */
	public DNSBL getDNSBL(){
		return this.dnsbl;
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
        if (mute != null) {
            return mute;
        }
        TempMute tempm = tempmutes.get(name);
        if (tempm !=null) {
            if (System.currentTimeMillis() < tempm.getExpires()) {
                return tempm;
            }
            else{
            	tempmutes.remove(name);
            	
            	db.execute("DELETE FROM mutes WHERE name = ? AND expires <> 0", name);
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
    			db.execute("DELETE FROM bans WHERE name = ? AND expires <> 0", name);
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
    		if(System.currentTimeMillis() < tempIPBan.getExpires()){
            	return tempIPBan;
    		}
    		else{
    			ipbans.remove(ip);
    			db.execute("DELETE FROM ipbans WHERE ip = ? AND expires <> 0", ip);
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
     * Fetches the IP history of everyone ever
     * @return the IP history of everyone ever. Format: HashMap<Username, IP Address>.
     */
    public HashMap<String, String> getIPHistory(){
    	return this.recentips;
    }
    
    /**
     * Returns a HashSet of lower case users which have joined from the given IP address
     * @param ip The IP to lookup
     * @return a HashSet of lower case users which have joined from the given IP address
     */
    public HashSet<String> getUsers(String ip){
    	return this.iplookup.get(ip);
    }
    
    /**
     * Fetches a list of all warnings the player currently has to their name.
     * @param name The name of the player to fetch. Case insensitive.
     * @return a list of all warnings the player currently has to their name.
     */
    public List<Warn> getWarnings(String name){
    	name = name.toLowerCase();
    	List<Warn> warnings = this.warnings.get(name);
    	
    	if(warnings == null) return null; //No warnings, return an empty list.
    	
    	Iterator<Warn> it = warnings.iterator();
    	while(it.hasNext()){
    		//Expire old warnings
    		Warn w = it.next();
    		if(w.getExpires() < System.currentTimeMillis()){
    			it.remove();
    		}
    	}
    	
    	return warnings;
    }
    
    /**
     * Creates a new ban and stores it in the database
     * @param name The name of the player who is banned
     * @param reason The reason they were banned
     * @param banner The admin who banned them
     */
    public void ban(String name, String reason, String banner){
    	name = name.toLowerCase();
    	banner = banner.toLowerCase();
    	
    	Ban ban = new Ban(reason, banner, System.currentTimeMillis());
    	this.bans.put(name, ban);
    	
    	db.execute("INSERT INTO bans (name, reason, banner, time) VALUES (?, ?, ?, ?)", name, reason, banner, System.currentTimeMillis());
    }
    
    /**
     * Removes a ban and removes it from the database
     * @param name The name of the player who is banned
     */
    public void unban(String name){
    	name = name.toLowerCase();
    	Ban ban = this.bans.get(name);
    	TempBan tBan = this.tempbans.get(name);
    	String ip = this.getIP(name);
    	
    	if(ban != null){
    		this.bans.remove(name);
    		db.execute("DELETE FROM bans WHERE name = ?", name);
    	}
    	if(tBan != null){
    		this.tempbans.remove(name);
    		if(ban == null){
    			//We still need to run this query then.
    			db.execute("DELETE FROM bans WHERE name = ?", name);
    		}
    	}
    	unbanip(ip);
    }
    
    /**
     * Removes a ban and removes it from the database
     * @param ip The ip of the player who is banned
     */
    public void unbanip(String ip){
    	IPBan ipBan = this.ipbans.get(ip);
    	TempIPBan tipBan = this.tempipbans.get(ip);
    	
    	if(ipBan != null){
    		this.ipbans.remove(ip);
    		db.execute("DELETE FROM ipbans WHERE ip = ?", ip);
    	}
    	if(tipBan != null){
    		this.tempipbans.remove(ip);
    		if(ipBan == null){
    			//We still need to delete it from the database
    			db.execute("DELETE FROM ipbans WHERE ip = ?", ip);
    		}
    	}
    }
    
    /**
     * Unmutes the given player.
     * @param name The name of the player. Case insensitive.
     */
    public void unmute(String name){
    	name = name.toLowerCase();
    	
    	Mute mute = this.mutes.get(name);
    	TempMute tMute = this.tempmutes.get(name);
    	
    	//Escape it
    	if(mute != null){
    		this.mutes.remove(name);
    		db.execute("DELETE FROM mutes WHERE name = ?", name);
    	}
    	if(tMute != null){
    		this.tempmutes.remove(name);
    		if(mute == null){
    			//We still need to delete the mute from the database
    			db.execute("DELETE FROM mutes WHERE name = ?", name);
    		}
    	}
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
    	
    	db.execute("INSERT INTO bans (name, reason, banner, time, expires) VALUES (?, ?, ?, ?, ?)", name, reason, banner, System.currentTimeMillis(), expires);
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
    	
    	db.execute("INSERT INTO ipbans (ip, reason, banner, time) VALUES (?, ?, ?, ?)", ip, reason, banner, System.currentTimeMillis());
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
    	
    	db.execute("INSERT INTO ipbans (ip, reason, banner, time, expires) VALUES (?, ?, ?, ?, ?)", ip, reason, banner, System.currentTimeMillis(), expires);
    }
    
    /**
     * Mutes a player so they can't chat.
     * @param name The name of the player to mute
     * @param banner The admin who muted them
     */
    public void mute(String name, String banner){
    	name = name.toLowerCase();
    	
    	Mute mute = new Mute(banner, System.currentTimeMillis());
    	this.mutes.put(name, mute);
    	
    	db.execute("INSERT INTO mutes (name, muter, time) VALUES (?, ?, ?)", name, banner, System.currentTimeMillis());
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
    	
    	db.execute("INSERT INTO mutes (name, muter, time, expires) VALUES (?, ?, ?, ?)", name, banner, System.currentTimeMillis(), expires);
    }
    
    /**
     * Gives a player a warning
     * @param name The name of the player
     * @param reason The reason for the warning
     */
    public void warn(String name, String reason, String banner){
    	name = name.toLowerCase();
    	banner = banner.toLowerCase();
    	long expires = System.currentTimeMillis() + plugin.getConfig().getInt("warn-expirey-in-minutes") * 60000;
    	
    	List<Warn> warns = this.getWarnings(name);
    	
    	if(warns == null){
    		warns = new ArrayList<Warn>();
    		this.warnings.put(name, warns);
    	}
    	
    	//Adds it to warnings
    	warns.add(new Warn(reason, banner, expires));
    	
    	db.execute("INSERT INTO warnings (name, reason, banner, expires) VALUES (?, ?, ?, ?)", name, reason, banner, expires);
    	
    	int maxWarns = plugin.getConfig().getInt("max-warnings");
    	if(maxWarns <= 0) return;
    	
    	int warnsSize = warns.size();
    	if(warnsSize != 0 && warnsSize % maxWarns == 0){
    		this.tempban(name, "Reached Max Warnings:\n" + reason, banner, System.currentTimeMillis() + 3600000); //1 hour
    		Player p = Bukkit.getPlayerExact(name);
    		if(p != null){
    			p.kickPlayer("Reached Max Warnings:\n" + reason);
    		}
    		announce(Formatter.secondary + name + Formatter.primary + " has reached max warnings.  One hour ban.");
    	}
    }
    
    /**
     * Removes all warnings for a player from memory and the database
     * @param name The name of the player. Case insensitive.
     */
    public void clearWarnings(String name){
    	name = name.toLowerCase();
    	
    	this.warnings.remove(name);
    	
    	db.execute("DELETE FROM warnings WHERE name = ?", name);
    }
    
    /**
     * Gets the IP address a player last used, even if offline
     * Will return null if no history for that IP address.
     * @param user The player to look up
     * @return The last IP they used to connect to the server, or null if they've never connected
     */
    public String getIP(String user){
    	return this.recentips.get(user.toLowerCase());
    }
    
    /**
     * Notes that a player joined from the given IP.
     * @param name Name of player. Case insensitive.
     * @param ip The ip they're connecting from.
     */
    public void logIP(String name, String ip){
    	name = name.toLowerCase();
    	String oldIP = this.recentips.get(name);
    	if(ip.equals(oldIP)) return; //Nothing has changed.
    	
    	if(!this.recentips.containsKey(name)){
    		//First time we've seen this player! Add them to the autocomplete list
    		this.players.add(name);
    	}
    	else{
    		//List shouldn't be null, because they've been recorded already
    		HashSet<String> list = this.iplookup.get(oldIP);
    		list.remove(name);
    	}
    	
    	//Get the list of users on their ip
    	HashSet<String> list = this.iplookup.get(ip);
    	if(list == null){
    		//No history, so add the history object
    		list = new HashSet<String>();
    		this.iplookup.put(ip, list);
    	}
    	//Add them to the new or old history
    	list.add(name);
    	
    	if(this.recentips.containsKey(name)){
    		//query = "UPDATE iphistory SET ip = '"+ip+"' WHERE name = '"+db.escape(name)+"'";
    		db.execute("UPDATE iphistory SET ip = ? WHERE name = ?", ip, name);
    	}
    	else{
    		db.execute("INSERT INTO iphistory (name, ip) VALUES (?, ?)", name, ip);
    	}
    	
    	this.recentips.put(name, ip);
    }
    
    /**
     * Notes that a player joined from the given IP.
     * @param p The player who is connecting
     */
    public void logIP(Player p){
    	this.logIP(p.getName(), p.getAddress().getAddress().getHostAddress());
    }
	
	/**
	 * Announces a message to the whole server.
	 * Also logs it to the console.
	 * @param s The string
	 */
	public void announce(String s){
		announce(s, false, null);
	}
	/**
	 * Announces a message to everyone who has the given permission
	 * Also logs it to the console.
	 * @param s The string
	 * @param silent If this message is to be silent
	 */
	public void announce(String s, boolean silent, CommandSender sender){
		if(silent){
			s = Formatter.primary + "[Silent] " + s;
			
			for(Player p : Bukkit.getOnlinePlayers()){
				if(p.hasPermission("maxbans.seesilent")) p.sendMessage(s);
			}
			if(sender != null && !sender.hasPermission("maxbans.seesilent")){
				sender.sendMessage(s);
			}
		}
		else{
			for(Player p : Bukkit.getOnlinePlayers()){
				if(p.hasPermission("maxbans.seebroadcast")) p.sendMessage(s);
			}
		}
		plugin.getLogger().info(s);
	}
	
	/**
	 * Finds the nearest known match to a given name.
	 * Searches online players first, then any exact matches
	 * for offline players, and then the nearest match for
	 * offline players.
	 * 
	 * @param partial The partial name
	 * @return The full name, or the same partial name if it can't find one
	 * <br/><br/>
	 * This is the equivilant of match(partial, false)
	 */
	public String match(String partial){
		return match(partial, false);
	}
	/**
	 * Finds the nearest known match to a given name.
	 * Searches online players first, then any exact matches
	 * for offline players, and then the nearest match for
	 * offline players.
	 * 
	 * @param partial The partial name
	 * @param excludeOnline Avoids searching online players if true
	 * @return The full name, or the same partial name if it can't find one
	 */
	public String match(String partial, boolean excludeOnline){
		partial = partial.toLowerCase();
		//Check the name isn't already complete
		String ip = this.recentips.get(partial);
		if(ip != null) return partial; // it's already complete.
		
		//Check the player and if they're online
		if(excludeOnline == false){
			Player p = Bukkit.getPlayer(partial);
			if(p != null) return p.getName();
		}
		
		//Scan the map for the match. Iff one is found, return it.
		String nearestMap = players.nearestKey(partial); // Note that checking the nearest match to an exact name will return the same exact name
		
		if(nearestMap != null) return nearestMap;
		
		//We can't help you. Maybe you can not be lazy.
		return partial;
	}
	
	/**
	 * Returns true if the server is disallowing all connections, except ones with maxbans.lockdown.bypass permissions.
	 * @return true if the server is disallowing all connections, except ones with maxbans.lockdown.bypass permissions.
	 */
	public boolean isLockdown(){
		return this.lockdown;
	}
	/**
	 * The reason the server is in lockdown. Result is not guaranteed if server is not in lockdown.
	 * @return The reason.
	 */
	public String getLockdownReason(){
		return this.lockdownReason;
	}
	
	/**
	 * Changes the lockdown mode of the server.
	 * @param lockdown Whether or not the server should go into lockdown.
	 * @param reason The reason it is going into lockdown. This is ignored if unlocking the server.
	 */
	public void setLockdown(boolean lockdown, String reason){
		this.lockdown = lockdown;
		reason = ChatColor.translateAlternateColorCodes('&', reason);
		if(lockdown){
			plugin.getConfig().set("lockdown", true);
			plugin.getConfig().set("lockdown-reason", reason);
			this.lockdownReason = reason;
		}
		else{
			plugin.getConfig().set("lockdown", false);
			plugin.getConfig().set("lockdown-reason", "");
			this.lockdownReason = "";
		}
		plugin.saveConfig();
	}
	/**
	 * Sets the lockdown status, with a default "Maintenance" message.
	 * @param lockdown Whether to lock down or not.
	 */
	public void setLockdown(boolean lockdown){
		setLockdown(lockdown, "Maintenance");
	}
	
	/**
	 * Registers the given string as a chat command, like /me or /say.
	 * This command will be blocked from muted players.
	 * @param s The command string, excluding the starting slash (/)
	 */
	public void addChatCommand(String s){
		s = s.toLowerCase();
		this.chatCommands.add(s);
	}
	/**
	 * Returns true if the given command is blocked from muted players.
	 * @param s The command to look up, excluding the starting slash (/).
	 * @return True if muted players cannot use the command.
	 */
	public boolean isChatCommand(String s){
		s = s.toLowerCase();
		return this.chatCommands.contains(s);
	}
}
