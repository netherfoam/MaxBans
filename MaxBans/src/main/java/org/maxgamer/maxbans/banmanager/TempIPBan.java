package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

public class TempIPBan extends IPBan implements Temporary
{
    private long expires;
    
    public TempIPBan(final String ip, final String reason, final String banner, final long created, final long expires) {
        super(ip, reason, banner, created);
        this.expires = expires;
    }
    
    public long getExpires() {
        return this.expires;
    }
    
    public boolean hasExpired() {
        return System.currentTimeMillis() > this.expires;
    }
    
    public String getKickMessage() {
        return Msg.get("disconnection.you-are-temp-ipbanned", new String[] { "reason", "banner", "time", "appeal-message" }, new String[] { this.getReason(), this.getBanner(), Util.getTimeUntil(this.expires), MaxBans.instance.getBanManager().getAppealMessage() });
    }
}
