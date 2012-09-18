package org.maxgamer.maxbans.banmanager;

public class TempIPBan extends IPBan{
    private long expires;
    
    /**
     * Creates a new Temp IP Ban - Does not store it in memory or the database
     * @param reason The reason for the ban
     * @param banner The admin who banned them
     * @param time The time the ban was created
     * @param expires The time the ban expires
     */
    public TempIPBan(String reason, String banner, long time, long expires){
        super (reason, banner, time);
        this.expires=expires;
    }
    
    /**
     * Returns the time that the ban expires.
     * @return the time that the ban expires.
     */
    public long getExpires() {
        return expires;
    }
}