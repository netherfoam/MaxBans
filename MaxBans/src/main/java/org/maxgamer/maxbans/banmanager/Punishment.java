package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.util.Util;

public class Punishment
{
    protected String id;
    protected String reason;
    protected String banner;
    protected long created;
    
    public Punishment(final String id, final String reason, final String banner, final long created) {
        super();
        this.id = id;
        this.reason = reason;
        this.banner = banner;
        this.created = created;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getReason() {
        return this.reason;
    }
    
    public String getBanner() {
        return this.banner;
    }
    
    public long getCreated() {
        return this.created;
    }
    
    public String toString() {
        String str = String.valueOf(this.getClass().getSimpleName()) + " ID:" + this.id + ((this.reason != null && !this.reason.isEmpty()) ? (", R:" + this.reason) : "") + ", B:" + this.banner + ", C:" + this.created;
        if (this instanceof Temporary) {
            final Temporary t = (Temporary)this;
            str = String.valueOf(str) + ", E:" + Util.getTimeUntil(t.getExpires());
        }
        return str;
    }
}
