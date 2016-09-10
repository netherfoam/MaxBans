package org.maxgamer.maxbans.banmanager;

public class HistoryRecord
{
    private String name;
    private String banner;
    private String message;
    private long created;
    
    public HistoryRecord(final String name, final String banner, final String message) {
        this(name, banner, message, System.currentTimeMillis());
    }
    
    public HistoryRecord(final String name, final String banner, final String message, final long created) {
        super();
        this.message = message;
        this.created = created;
        this.banner = banner;
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getBanner() {
        return this.banner;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public long getCreated() {
        return this.created;
    }
    
    public String toString() {
        return this.message;
    }
}
