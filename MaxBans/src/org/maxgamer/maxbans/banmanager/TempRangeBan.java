package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.Util;

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
		return Msg.get("disconnection.you-are-temp-rangebanned", new String[]{"reason", "banner", "appeal-message", "range", "time"}, new String[]{getReason(), getBanner(), MaxBans.instance.getBanManager().getAppealMessage(), this.toString(), Util.getTimeUntil(expires)});
	}
}