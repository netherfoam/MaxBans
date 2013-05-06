package org.maxgamer.maxbans.util;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Temporary;

public class TempRangeBan extends RangeBan implements Temporary{
	private long expires;
	public TempRangeBan(String banner, String reason, long created, long expires, IPAddress start, IPAddress end) {
		super(banner, reason, created, start, end);
		this.expires = expires;
	}

	@Override
	public long getExpires() {
		return expires;
	}

	@Override
	public boolean hasExpired() {
		return System.currentTimeMillis() > expires;
	}

	/**
	 * You're banned!<br/>
	 * Reason: 'Misconduct'<br/>
	 * By Console.
	 */
	@Override
	public String getKickMessage(){
		StringBuilder sb = new StringBuilder(50);
		sb.append(Formatter.regular + "Your IP Address (" + Formatter.secondary + this.toString() + Formatter.regular + ") is RangeBanned.\n");
		sb.append("The ban expires in " + Formatter.time + Util.getTimeUntil(this.getExpires()) + Formatter.regular + ".\n"); 
		sb.append(Formatter.regular + "Reason: " + Formatter.reason + this.getReason() + "\n");
		sb.append(Formatter.regular + "By: " + Formatter.banner + this.getBanner());
		
		 //Append the appeal message, if necessary.
        String appeal = MaxBans.instance.getBanManager().getAppealMessage();
        if(appeal != null && appeal.isEmpty() == false){
        	reason += "\n" + Formatter.regular + appeal;
        }
        return sb.toString();
	}
}