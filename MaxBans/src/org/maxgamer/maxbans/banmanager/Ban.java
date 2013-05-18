package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;

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
	
	/**
	 * You're banned!<br/>
	 * Reason: 'Misconduct'<br/>
	 * By Console.
	 */
	public String getKickMessage(){
		/*
		StringBuilder sb = new StringBuilder(50);
		sb.append(Formatter.message + "You're banned!" + Formatter.regular + "\n Reason: '");
		sb.append(Formatter.reason + reason);
		sb.append(Formatter.regular + "'\n By ");
		sb.append(Formatter.banner + banner + Formatter.regular + ". ");
		
		String appeal = MaxBans.instance.getBanManager().getAppealMessage();
        if(appeal != null && appeal.isEmpty() == false){
        	sb.append("\n" + Formatter.regular + appeal);
        }
        return sb.toString();*/
		return Msg.get("disconnection.you-are-banned", new String[]{"reason", "banner", "appeal-message"}, new String[]{getReason(), getBanner(), MaxBans.instance.getBanManager().getAppealMessage()});
	}
}