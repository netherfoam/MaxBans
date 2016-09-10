package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.IPAddress;

public class RangeBan extends Punishment implements Comparable<RangeBan>
{
    private IPAddress start;
    private IPAddress end;
    
    public RangeBan(final String banner, final String reason, final long created, final IPAddress start, final IPAddress end) {
        super(start + "-" + end, reason, banner, created);
        if (start.compareTo(end) > 0) {
            this.start = end;
            this.end = start;
        }
        else {
            this.start = start;
            this.end = end;
        }
    }
    
    public IPAddress getStart() {
        return this.start;
    }
    
    public IPAddress getEnd() {
        return this.end;
    }
    
    public int compareTo(final RangeBan rb) {
        return this.start.compareTo(rb.start);
    }
    
    public int hashCode() {
        return this.start.hashCode();
    }
    
    public boolean equals(final Object o) {
        if (o instanceof RangeBan) {
            final RangeBan rb = (RangeBan)o;
            return this.start.equals(rb.start) && this.end.equals(rb.end);
        }
        return false;
    }
    
    public String toString() {
        return String.valueOf(this.start.toString()) + "-" + this.end.toString();
    }
    
    public boolean overlaps(final RangeBan rb) {
        return this.start.compareTo(rb.end) != this.end.compareTo(rb.start);
    }
    
    public boolean contains(final IPAddress address) {
        return address.compareTo(this.start) >= 0 && address.compareTo(this.end) <= 0;
    }
    
    public String getKickMessage() {
        return Msg.get("disconnection.you-are-rangebanned", new String[] { "reason", "banner", "appeal-message", "range" }, new String[] { this.getReason(), this.getBanner(), MaxBans.instance.getBanManager().getAppealMessage(), this.toString() });
    }
}
