package org.maxgamer.maxbans.banmanager;

/**
 * The ban class
 * This will be stored in a hashmap in BanManager <String, Ban>
 * A ban is a ban, and not an ip ban or a temporary ban.  
 *
 */
public class Ban extends Punishment{
    /**
     * 
     * @param reason The reason for the ban
     * @param banner The banner
     * @param time The time they were banned
     */
	public Ban(String user, String reason, String banner, long created){
		super(user, reason, banner, created);
	}
}