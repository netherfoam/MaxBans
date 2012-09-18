package org.maxgamer.maxbans.banmanager;

/**
 * The ban class
 * This will be stored in a hashmap in BanManager <String, Ban>
 * A ban is a ban, and not an ip ban or a temporary ban.  
 *
 */
public class Ban{
    private String reason;
    private String banner;
    private long time;
    
    /**
     * 
     * @param reason The reason for the ban
     * @param banner The banner
     * @param time The time they were banned
     */
	public Ban(String reason, String banner, long time){
		this.reason=reason;
        this.banner=banner;
        this.time=time;
	}
	
	/**
	 * @return The string reason why they were banned
	 */
	public String getReason(){
		return reason;
	}
    
	/**
	 * @return Returns the name of the admin who banned them
	 */
	public String getBanner(){
		return banner;
	}
    
	/**
	 * @return Returns the time they were banned
	 */
	public long getTime(){
		return time;
	}
}