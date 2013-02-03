package org.maxgamer.maxbans.banmanager;

public class IPBan extends Ban{ // I think its just luck that you can do this.
	/**
	 * Creates a new IP ban. Does not store it in memory or DB.
	 * @param reason The reason for the ban
	 * @param banner The admin who banned them
	 * @param time The time the ban was created
	 */
	public IPBan(String reason, String banner, long time){
        super(reason,banner,time);
	}
	
	@Override
	public String toString(){
		return "{IPBAN} Reason: " + super.getReason();
	}
}