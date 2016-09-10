package org.maxgamer.maxbans.banmanager;

import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.sync.Packet;
import org.maxgamer.maxbans.MaxBans;

public class SyncBanManager extends BanManager
{
    private boolean sync;
    
    public SyncBanManager(final MaxBans plugin) {
        super(plugin);
        this.sync = true;
    }
    
    public void startSync() {
        this.sync = false;
    }
    
    public void stopSync() {
        this.sync = true;
    }
    
    public void addHistory(final String name, final String banner, final String message) {
        super.addHistory(name, banner, message);
        if (this.sync) {
            final Packet p = new Packet("addhistory").put("string", message).put("banner", banner).put("name", name);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public RangeBan ban(final RangeBan rb) {
        final RangeBan b = super.ban(rb);
        if (this.sync) {
            final Packet p = new Packet("rangeban").put("reason", rb.getReason()).put("start", rb.getStart().toString()).put("end", rb.getEnd().toString()).put("banner", rb.getBanner()).put("created", rb.getCreated());
            super.plugin.getSyncer().broadcast(p);
        }
        return b;
    }
    
    public void unban(final RangeBan rb) {
        super.unban(rb);
        if (this.sync) {
            final Packet p = new Packet("rangeban").put("start", rb.getStart().toString()).put("end", rb.getEnd().toString());
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void setWhitelisted(final String name, final boolean white) {
        super.setWhitelisted(name, white);
        if (this.sync) {
            final Packet p = new Packet("whitelist").put("name", name);
            if (white) {
                p.put("white", "");
            }
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void kick(final String user, final String msg) {
        super.kick(user, msg);
        if (this.sync) {
            final Packet p = new Packet("kick").put("name", user).put("reason", msg);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void kickIP(final String ip, final String msg) {
        super.kickIP(ip, msg);
        if (this.sync) {
            final Packet p = new Packet("kickip").put("ip", ip).put("reason", msg);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public boolean setImmunity(final String user, final boolean immune) {
        final boolean change = super.setImmunity(user, immune);
        if (this.sync) {
            final Packet p = new Packet("setimmunity").put("name", user);
            if (immune) {
                p.put("immune", "");
            }
            super.plugin.getSyncer().broadcast(p);
        }
        return change;
    }
    
    public void unban(final String name) {
        super.unban(name);
        if (this.sync) {
            final Packet p = new Packet("unban").put("name", name);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void unbanip(final String ip) {
        super.unbanip(ip);
        if (this.sync) {
            final Packet p = new Packet("unbanip").put("ip", ip);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void unmute(final String name) {
        super.unmute(name);
        if (this.sync) {
            final Packet p = new Packet("unmute").put("name", name);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void tempban(final String name, final String reason, final String banner, final long expires) {
        super.tempban(name, reason, banner, expires);
        if (this.sync) {
            final Packet p = new Packet("tempban").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void ipban(final String ip, final String reason, final String banner) {
        super.ipban(ip, reason, banner);
        if (this.sync) {
            final Packet p = new Packet("ipban").put("ip", ip).put("reason", reason).put("banner", banner);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void tempipban(final String ip, final String reason, final String banner, final long expires) {
        super.tempipban(ip, reason, banner, expires);
        if (this.sync) {
            final Packet p = new Packet("tempipban").put("ip", ip).put("reason", reason).put("banner", banner).put("expires", expires);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void mute(final String name, final String banner, final String reason) {
        super.mute(name, banner, reason);
        if (this.sync) {
            final Packet p = new Packet("mute").put("name", name).put("reason", reason).put("banner", banner);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void tempmute(final String name, final String banner, final String reason, final long expires) {
        super.tempmute(name, banner, reason, expires);
        if (this.sync) {
            final Packet p = new Packet("tempmute").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void warn(final String name, final String reason, final String banner) {
        super.warn(name, reason, banner);
        if (this.sync) {
            final Packet p = new Packet("warn").put("name", name).put("reason", reason).put("banner", banner);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public void clearWarnings(final String name) {
        super.clearWarnings(name);
        if (this.sync) {
            final Packet p = new Packet("clearwarnings").put("name", name);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public boolean logActual(final String name, final String actual) {
        final boolean change = super.logActual(name, actual);
        if (this.sync) {
            final Packet p = new Packet("setname").put("name", name);
            super.plugin.getSyncer().broadcast(p);
        }
        return change;
    }
    
    public boolean logIP(final String name, final String ip) {
        final boolean change = super.logIP(name, ip);
        if (this.sync) {
            final Packet p = new Packet("setip").put("name", name).put("ip", ip);
            super.plugin.getSyncer().broadcast(p);
        }
        return change;
    }
    
    public void announce(final String s, final boolean silent, final CommandSender sender) {
        super.announce(s, silent, sender);
        if (this.sync) {
            final Packet p = new Packet("announce").put("string", s);
            if (silent) {
                p.put("silent", "true");
            }
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public boolean deleteWarning(final String name, final Warn warn) {
        if (super.deleteWarning(name, warn)) {
            if (this.sync) {
                final Packet p = new Packet("unwarn").put("name", name);
                super.plugin.getSyncer().broadcast(p);
            }
            return true;
        }
        return false;
    }
    
    public void ban(final String name, final String reason, final String banner) {
        super.ban(name, reason, banner);
        if (this.sync) {
            final Packet p = new Packet("ban").put("name", name).put("reason", reason).put("banner", banner);
            super.plugin.getSyncer().broadcast(p);
        }
    }
    
    public String toString() {
        return "SyncBanManager:" + super.toString();
    }
}
