package org.maxgamer.maxbans.banmanager;

public class TempMute extends Mute implements Temporary{
    private long expires;
    
    /**
     * Creates a new temporary mute. Does not store it in the DB or memory cache.
     * @param muter The admin who muted them
     * @param time The time the mute was created
     * @param expires The time the mute expires
     */
	public TempMute(String muted, String muter, String reason, long created, long expires){
		super(muted, muter, reason, created);
        this.expires = expires;
	}
    
	/**
	 * Returns the time that the mute expires
	 * @return the time that the mute expires
	 */
    public long getExpires() {
        return expires;
    }
    
    public boolean hasExpired(){
    	return System.currentTimeMillis() > expires;
    }
}