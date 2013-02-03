package org.maxgamer.maxbans.banmanager;

public class TempMute extends Mute{
    private long expires;
    
    /**
     * Creates a new temporary mute. Does not store it in the DB or memory cache.
     * @param muter The admin who muted them
     * @param time The time the mute was created
     * @param expires The time the mute expires
     */
	public TempMute(String muter, long time, long expires){
		super (muter, time);
        this.expires = expires;
	}
    
	/**
	 * Returns the time that the mute expires
	 * @return the time that the mute expires
	 */
    public long getExpires() {
        return expires;
    }
    
	@Override
	public String toString(){
		return "{TEMPMUTE} Muter: " + getMuter();
	}
}