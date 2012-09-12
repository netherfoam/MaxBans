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
    private long timeOfBan;
    
	public Ban(String reason, String banner, long time){
		this.reason=reason;
                this.banner=banner;
                this.timeOfBan=time;
	}
	
	public String getReason(){
		return reason;
	}
        
	public String getBanner(){
		return banner;
	}
        
	public long getTimeOfBan(){
		return timeOfBan;
	}
}