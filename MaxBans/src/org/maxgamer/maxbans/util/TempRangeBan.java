package org.maxgamer.maxbans.util;

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
	
}