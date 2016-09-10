package org.maxgamer.maxbans.banmanager;

public class Warn
{
    private String reason;
    private String banner;
    private long expires;
    
    public Warn(final String reason, final String banner, final long expires) {
        super();
        this.reason = reason;
        this.banner = banner;
        this.expires = expires;
    }
    
    public String getReason() {
        return this.reason;
    }
    
    public String getBanner() {
        return this.banner;
    }
    
    public long getExpires() {
        return this.expires;
    }
}
