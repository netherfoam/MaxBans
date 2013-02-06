package org.maxgamer.maxbans.banmanager;

public class TempBan extends Ban implements Temporary{
    private long expires;
        
    /**
     * Creates a new tempban. Does not store it in memory or the DB.
     * @param reason The reason for the ban
     * @param banner The admin who banned him
     * @param created The time the ban was created
     * @param expires The time the ban will expires
     */
	public TempBan(String user, String reason, String banner, long created, long expires){
		super(user, reason, banner, created);
        this.expires = expires;
	}

	/**
	 * Returns the time the ban expires
	 * @return The time the ban expires
	 */
    public long getExpires() {
        return expires;
    }
	/**
	 * Returns true if this tempban has expired.
	 * @return true if this tempban has expired.
	 */
	public boolean hasExpired(){
		return System.currentTimeMillis() > expires;
	}
}