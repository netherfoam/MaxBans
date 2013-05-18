package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Util;

public class TempIPBan extends IPBan implements Temporary{
    private long expires;
    
    /**
     * Creates a new Temp IP Ban - Does not store it in memory or the database
     * @param reason The reason for the ban
     * @param banner The admin who banned them
     * @param time The time the ban was created
     * @param expires The time the ban expires
     */
    public TempIPBan(String ip, String reason, String banner, long created, long expires){
    	super(ip, reason, banner, created);
        this.expires = expires;
    }
    
    /**
     * Returns the time that the ban expires.
     * @return the time that the ban expires.
     */
    public long getExpires() {
        return expires;
    }
    
    public boolean hasExpired(){
    	return System.currentTimeMillis() > expires;
    }
    
	/**
	 * You're banned!<br/>
	 * Reason: 'Misconduct'<br/>
	 * By Console.
	 */
	@Override
	public String getKickMessage(){
		/*
		StringBuilder sb = new StringBuilder(50);
		sb.append(Formatter.message + "You're temporarily IP Banned!" + Formatter.regular + "\n Reason: '");
		sb.append(Formatter.reason + reason);
		sb.append(Formatter.regular + "'\n By ");
		sb.append(Formatter.banner + banner + Formatter.regular + ". ");
		
    	sb.append("Expires in " + Formatter.time + Util.getTimeUntil(expires));
		
		String appeal = MaxBans.instance.getBanManager().getAppealMessage();
        if(appeal != null && appeal.isEmpty() == false){
        	sb.append("\n" + Formatter.regular + appeal);
        }
        return sb.toString();*/
		return Msg.get("disconnection.you-are-temp-ipbanned", new String[]{"reason", "banner", "time", "appeal-message"}, new String[]{getReason(), getBanner(), Util.getTimeUntil(expires), MaxBans.instance.getBanManager().getAppealMessage()});
	}
}