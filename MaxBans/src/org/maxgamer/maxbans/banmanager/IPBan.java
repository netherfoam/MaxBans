package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;

public class IPBan extends Ban{ // I think its just luck that you can do this.
	/**
	 * Creates a new IP ban. Does not store it in memory or DB.
	 * @param reason The reason for the ban
	 * @param banner The admin who banned them
	 * @param time The time the ban was created
	 */
	public IPBan(String ip, String reason, String banner, long created){
        super(ip, reason, banner, created);
	}
	
	@Override
	public String getKickMessage(){
		/*
	}
        StringBuilder sb = new StringBuilder(50); //kickmessage
        sb.append(Formatter.message + "You're IP Banned!" + Formatter.regular + "\n Reason: '");
		sb.append(Formatter.reason + reason);
		sb.append(Formatter.regular + "'\n By ");
		sb.append(Formatter.banner + banner + Formatter.regular + ". ");
		
		String appeal = MaxBans.instance.getBanManager().getAppealMessage();
        if(appeal != null && appeal.isEmpty() == false){
        	sb.append("\n" + Formatter.regular + appeal);
        }
        return sb.toString();*/
		
		return Msg.get("disconnection.you-are-ipbanned", new String[]{"reason", "banner", "appeal-message"}, new String[]{getReason(), getBanner(), MaxBans.instance.getBanManager().getAppealMessage()});
	}
}