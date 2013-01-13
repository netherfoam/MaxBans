package org.maxgamer.maxbans.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;

import java.util.Hashtable;

import sun.net.dns.ResolverConfiguration;

public class DNSBL{
	private HashMap<String, CacheRecord> history = new HashMap<String, CacheRecord>();
	/** How long it takes cache records to expire */
	private static long cache_timeout = 604800000L;
	
	private DirContext ictx;
	private ArrayList<String> lookupServices = new ArrayList<String>();
	private static String[] RECORD_TYPES = {"A"};
	
	private Database db;
	private MaxBans plugin;
	
	public DNSBL(MaxBans plugin) throws NamingException{
		this.plugin = plugin;
		this.db = plugin.getDB();
		
		StringBuilder sb = new StringBuilder();
		
		List<?> nameservers = ResolverConfiguration.open().nameservers();
		for(Object dns : nameservers){
			sb.append("dns://" + dns + " ");
		}
		
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "4000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");
        env.put(Context.PROVIDER_URL, sb.toString());
        
        ictx = new InitialDirContext(env);
        
        List<String> cfgServers = this.plugin.getConfig().getStringList("dnsbl.servers");
        
        if(cfgServers != null){
        	this.lookupServices.addAll(cfgServers);
        }
        
        if(!db.hasTable("proxys")){
        	plugin.getLogger().info("Creating proxys table...");
        	db.createProxysTable();
        }
        else{
        	plugin.getLogger().info("Loading proxys...");
        	try{
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
	}
	
	/**
	 * Returns the lookup services array list.  You may edit this safely.
	 * @return the lookup services array list.
	 */
    public ArrayList<String> getLookupServices(){
    	return this.lookupServices;
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
    		db.execute("DELETE FROM proxys WHERE ip = ?", ip);
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
    public DNSStatus reload(String ip){    	
    	//Reverses the IP's order... Eg 192.168.2.4 becomes 4.2.168.192
        String[] parts = ip.split("\\.");
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < parts.length; i++){
            buffer.insert(0, '.');
            buffer.insert(0, parts[i]);
        }
        String reverse = buffer.toString();
        //End of reversal
        
        Attribute attribute;
        Attributes attributes;
        
        String lookupHost;
        
        //By default, allow
        CacheRecord r = new CacheRecord(DNSStatus.ALLOWED);
        
        for (String service : lookupServices){
            lookupHost = reverse + service;
            try{
                attributes = ictx.getAttributes(lookupHost, RECORD_TYPES);
                attribute = attributes.get("A");
                
                if (attribute != null ){
                	r = new CacheRecord(DNSStatus.DENIED);
                	break;
                }
            }
            catch (NameNotFoundException e){} //Host has never heard of that IP.
            catch (NamingException e){} //Crap... Our lookup failed?
        }
    	if(getRecord(ip) == null){ //Do we have an old record?
    		db.execute("INSERT INTO proxys (ip, status, created) VALUES (?, ?, ?)", ip, r.getStatus().toString(), r.getCreated()); //No old records
    	}
    	else{
    		db.execute("UPDATE proxys SET status = ?, created = ? WHERE ip = ?", r.getStatus().toString(), r.getCreated(), ip); //New record
    	}
    	
        this.history.put(ip, r);
        return r.status;
    }
    
    public class CacheRecord{
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
    }
    
    public enum DNSStatus{
    	ALLOWED,
    	DENIED,
    	UNKNOWN;
    }
}