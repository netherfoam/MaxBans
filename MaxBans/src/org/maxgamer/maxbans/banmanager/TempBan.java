package org.maxgamer.maxbans.banmanager;

public class TempBan extends Ban{
    private long expires;
        
    /**
     * Creates a new tempban. Does not store it in memory or the DB.
     * @param reason The reason for the ban
     * @param banner The admin who banned him
     * @param time The time the ban was created
     * @param expires The time the ban will expires
     */
	public TempBan(String reason, String banner, long time, long expires){
            super(reason, banner, time);
            this.expires = expires;
	}

	/**
	 * Returns the time the ban expires
	 * @return The time the ban expires
	 */
    public long getExpires() {
        return expires;
    }
}