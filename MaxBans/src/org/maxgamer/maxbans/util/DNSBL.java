package org.maxgamer.maxbans.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.sync.Packet;

public class DNSBL{
	private HashMap<String, CacheRecord> history = new HashMap<String, CacheRecord>();
	public HashMap<String, CacheRecord> getHistory(){
		return history;
	}
	
	private MaxBans plugin;
	
	/** How long it takes cache records to expire */
	private static long cache_timeout = 604800000L;
	/** The DNSBL servers to contact */
	private ArrayList<String> servers = new ArrayList<String>();

	/** Should we kick players who have proxy IPs? */
	private boolean kick = false;
	/** Should we notify online players with maxbans.notify perms when a DNSBL ip connects? */
	private boolean notify = true;
	
	public DNSBL(MaxBans plugin){
		this.plugin = plugin;
		Database db = plugin.getDB();
		
		kick = plugin.getConfig().getBoolean("dnsbl.kick");
		notify = plugin.getConfig().getBoolean("dnsbl.notify");
        
        List<String> cfgServers = this.plugin.getConfig().getStringList("dnsbl.servers");
        
        if(cfgServers != null){
        	this.servers.addAll(cfgServers);
        }
        
    	plugin.getLogger().info("Loading proxys...");
    	try{
    		db.getConnection().close();
    		//Purge old proxy records.
    		PreparedStatement ps = db.getConnection().prepareStatement("DELETE FROM proxys WHERE created < ?");
    		ps.setLong(1, (System.currentTimeMillis() - cache_timeout));
    		ps.execute(); //Must do this before pulling data.
    		
    		//Fetch valid proxy records.
    		ResultSet rs = db.getConnection().prepareStatement("SELECT * FROM proxys").executeQuery();
    		while(rs.next()){
    			String ip = rs.getString("ip");
    			String statusString = rs.getString("status");
    			long created = rs.getLong("created");
    			
    			DNSStatus status = DNSStatus.valueOf(statusString);
    			if(status == null){
    				plugin.getLogger().info("Invalid proxy status found: " + statusString);
    				db.execute("DELETE FROM proxys WHERE ip = ?", ip); //This happens later.
    				continue;
    			}
    			
    			CacheRecord r = new CacheRecord(status, created);
    			this.history.put(ip, r);
        	}
    	}
    	catch(SQLException e){
    		e.printStackTrace();
    		plugin.getLogger().info("Could not load proxys...");
    	}
	}
	
	/**
	 * Handles the given PlayerLoginEvent appropriately.
	 * If the IP is a known proxy, it will notify or kick
	 * the player immediately. If the IP has to be looked
	 * up, the event will be allowed, the IP will be looked
	 * up.  If the result is a proxy, then the player will
	 * be kicked soon after, and their IP cached for a week.
	 * @param event The PlayerLoginEvent.
	 */
	public void handle(PlayerLoginEvent event){
		if(event.getAddress() == null) return; //"Legacy purposes" says bukkit?
		handle(event.getPlayer(), event.getAddress().getHostAddress());
	}
	
	public void handle(final Player p, final String address){
    	CacheRecord r = getRecord(address);
    	
    	//We have no record of their IP, or it has expired.
    	if(r == null){
    		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){
				public void run() {
					//Fetch the new status, this method also caches it for future use.
					CacheRecord r = reload(address);
					
					if(plugin.getSyncer() != null){
						Packet packet = new Packet("dnsbl").put("ip", address);
						packet.put("status", r.getStatus().toString()).put("created", r.getCreated());
						
						plugin.getSyncer().broadcast(packet);
					}
					
					if(r.getStatus() == DNSStatus.DENIED){
						//Notify console.
						Bukkit.getScheduler().runTask(plugin, new Runnable(){
							public void run(){
								if(kick && p.isOnline()){
									String msg = Msg.get("disconnection.you-are-proxied", "ip", address);
									p.kickPlayer(msg);
								}
								if(notify){ 
									String msg = Formatter.secondary + p.getName() + Formatter.primary + " (" + Formatter.secondary + address + Formatter.primary + ") is joining from a proxy IP!";
									for(Player p : Bukkit.getOnlinePlayers()){
										if(p.hasPermission("maxbans.notify")){
											p.sendMessage(msg);
										}
									}
								}
								Bukkit.getLogger().info(p.getName() + " is using a proxy IP!");
							}
						});
					}
				}
    		});
    	}
    	else if(r.getStatus() == DNSStatus.DENIED){
    		if(notify){
    			String msg = Formatter.secondary + p.getName() + Formatter.primary + " (" + Formatter.secondary + address + Formatter.primary + ") is joining from a proxy IP!";
				for(Player pl : Bukkit.getOnlinePlayers()){
					if(pl.hasPermission("maxbans.notify")){
						pl.sendMessage(msg);
					}
				}
    		}
			Bukkit.getLogger().info(p.getName() + " is using a proxy IP!");
    		if(kick){
    			String msg = Msg.get("disconnection.you-are-proxied", "ip", address);
				p.kickPlayer(msg);
    			return;
    		}
    	}
	}
	
	/**
	 * Returns the lookup services array list.  You may edit this safely.
	 * @return the lookup services array list.
	 */
    public ArrayList<String> getServers(){
    	return this.servers;
    }
    
    /**
     * Fetches the record for the given IP from the cache.
     * This method will not use DNSBL.reload(ip), thus is
     * much faster. However, new IP's will always return null
     * until the IP has been used in DNSBL.reload(ip).
     * @param ip The IP address to lookup
     * @return The CacheRecord object.
     * 
     * At time of return, the CacheRecord will never be expired.
     */
    public CacheRecord getRecord(String ip){
    	CacheRecord r = this.history.get(ip);
    	if(r == null) return null;
    	if(r.hasExpired()){
    		plugin.getDB().execute("DELETE FROM proxys WHERE ip = ?", ip);
    		return null;
    	}
    	
    	return r;
    }
    
    /** 
     * Recaches the given IP Address from the DNSBL servers.
     * @param ip The IP to recache
     * @return The status of the IP.
     * This automatically caches the given IP address.
     */
    public CacheRecord reload(String ip){    	
    	//Reverses the IP's order... Eg 192.168.2.4 becomes 4.2.168.192
        String[] parts = ip.split("\\.");
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < parts.length; i++){
            buffer.insert(0, '.');
            buffer.insert(0, parts[i]);
        }
        String reverse = buffer.toString(); //Note, this also puts a trailling . on the end.
        //End of reversal
        
        //By default, allow
        CacheRecord r = new CacheRecord(DNSStatus.ALLOWED);
        
        for (String server : servers){
            try{
            	if(InetAddress.getByName(reverse + server) != null){
                	r = new CacheRecord(DNSStatus.DENIED);
                	break;
                }
            }
            catch (UnknownHostException e){} //Host has never heard of that IP...Safe.
        }
        
        setRecord(ip, r);
        return r;
    }
    
    /**
     * Sets that the given IP address, using the given record. Updates the database too.
     * @param ip The IP address
     * @param r The CacheRecord object.
     */
    public void setRecord(String ip, CacheRecord r){
    	if(getRecord(ip) == null){ //Do we have an old record?
    		plugin.getDB().execute("INSERT INTO proxys (ip, status, created) VALUES (?, ?, ?)", ip, r.getStatus().toString(), r.getCreated()); //No old records
    	}
    	else{
    		plugin.getDB().execute("UPDATE proxys SET status = ?, created = ? WHERE ip = ?", r.getStatus().toString(), r.getCreated(), ip); //New record
    	}
    	
        this.history.put(ip, r);
    }
    
    /**
     * Represents a DNS status, and the time it was acquired.
     * Used for caching, as DNS lookups are incredibly slow.
     */
    public static class CacheRecord{
    	private DNSStatus status;
    	private long created;
    	
    	/**
    	 * Represents a cache record
    	 * @param status The status type (Allowed, Denied, unknown)
    	 * @param created The time it was created.
    	 */
    	public CacheRecord(DNSStatus status, long created){
    		this.status = status;
    		this.created = created;
    	}
    	/**
    	 * Creates a cache record at the current time.
    	 * @param status The status type (Allowed, denied, unknown)
    	 */
    	public CacheRecord(DNSStatus status){
    		this(status, System.currentTimeMillis());
    	}
    	public DNSStatus getStatus(){
    		return status;
    	}
    	public long getCreated(){
    		return created;
    	}
    	public long getExpires(){
    		return getCreated() + cache_timeout;
    	}
    	public boolean hasExpired(){
    		return System.currentTimeMillis() > getExpires();
    	}
    	@Override
    	public String toString(){
    		return status.toString();
    	}
    }
    
    public enum DNSStatus{
    	ALLOWED,
    	DENIED,
    	UNKNOWN;
    }
}