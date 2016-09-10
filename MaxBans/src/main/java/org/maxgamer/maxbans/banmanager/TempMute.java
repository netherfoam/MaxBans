package org.maxgamer.maxbans.banmanager;

public class TempMute extends Mute implements Temporary
{
    private long expires;
    
    public TempMute(final String muted, final String muter, final String reason, final long created, final long expires) {
        super(muted, muter, reason, created);
        this.expires = expires;
    }
    
    public long getExpires() {
        return this.expires;
    }
    
    public boolean hasExpired() {
        return System.currentTimeMillis() > this.expires;
    }
}
