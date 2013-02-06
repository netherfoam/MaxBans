package org.maxgamer.maxbans.banmanager;

public class TempIPBan extends IPBan implements Temporary{
    private long expires;
    
    /**
     * Creates a new Temp IP Ban - Does not store it in memory or the database
     * @param reason The reason for the ban
     * @param banner The admin who banned them
     * @param time The time the ban was created
     * @param expires The time the ban expires
     */
    public TempIPBan(String ip, String reason, String banner, long created, long expires){
    	super(ip, reason, banner, created);
        this.expires = expires;
    }
    
    /**
     * Returns the time that the ban expires.
     * @return the time that the ban expires.
     */
    public long getExpires() {
        return expires;
    }
    
    public boolean hasExpired(){
    	return System.currentTimeMillis() > expires;
    }
}