package org.maxgamer.maxbans.banmanager;

import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.sync.Packet;

public class SyncBanManager extends BanManager{
	/**
	 * false if we should NOT send data back to the syncer (Eg, we are receiving data)
	 * true if we SHOULD send data to the syncer (Eg, WE requested a ban, notify other servers)
	 */
	private boolean sync = true;
	public SyncBanManager(MaxBans plugin) {
		super(plugin);
	}
	
	/**
	 * This method is invoked by the Syncer when it starts requesting the SyncBanManager to do things.
	 * The stopSync() should be called after methods are run.
	 */
	public void startSync(){
		sync = false;
	}
	/**
	 * This method is invoked by the Syncer when it stops requesting the SyncBanManager to do things.
	 * The startSync() should be called before methods are run, and then the methods, then this method.
	 */
	public void stopSync(){
		sync = true;
	}
	
	//Who the fuck needs comments?
	public void addHistory(String name, String banner, String message){
		if(sync){
			Packet p = new Packet("addhistory").put("string", message).put("banner", banner).put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		super.addHistory(name, banner, message);
	}
	
	public RangeBan ban(RangeBan rb){
		if(sync){
			Packet p = new Packet("rangeban").put("reason", rb.getReason()).put("start", rb.getStart().toString()).put("end", rb.getEnd().toString()).put("banner", rb.getBanner()).put("created", rb.getCreated());
			super.plugin.getSyncer().broadcast(p);
		}
		return super.ban(rb);
	}
	public void unban(RangeBan rb){
		if(sync){
			Packet p = new Packet("rangeban").put("start", rb.getStart().toString()).put("end", rb.getEnd().toString());
			super.plugin.getSyncer().broadcast(p);
		}
		super.unban(rb);
	}
	public void setWhitelisted(String name, boolean white){
		if(sync){
			Packet p = new Packet("whitelist").put("name", name);
			if(white){
				p.put("white", "");
			}
			super.plugin.getSyncer().broadcast(p);
		}
		super.setWhitelisted(name, white);
	}
	public boolean kick(String user, String msg){
		if(sync){
			Packet p = new Packet("kick").put("name", user).put("reason", msg);
			super.plugin.getSyncer().broadcast(p);
		}
		return super.kick(user, msg);
	}
	public boolean kickIP(String ip, String msg){
		if(sync){
			Packet p = new Packet("kickip").put("ip", ip).put("reason", msg);
			super.plugin.getSyncer().broadcast(p);
		}
		return super.kickIP(ip, msg);
	}
	public boolean setImmunity(String user, boolean immune){
		if(sync){
			Packet p = new Packet("setimmunity").put("name", user);
			if(immune){
				p.put("immune", "");
			}
			super.plugin.getSyncer().broadcast(p);
		}
		return super.setImmunity(user, immune);
	}
	
	public void unban(String name){
		if(sync){
			Packet p = new Packet("unban").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		super.unban(name);
	}
	public void unbanip(String ip){
		if(sync){
			Packet p = new Packet("unbanip").put("ip", ip);
			super.plugin.getSyncer().broadcast(p);
		}
		super.unbanip(ip);
	}
	public void unmute(String name){
		if(sync){
			Packet p = new Packet("unmute").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		super.unmute(name);
	}
	public void tempban(String name, String reason, String banner, long expires){
		if(sync){
			Packet p = new Packet("tempban").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
		super.tempban(name, reason, banner, expires);
	}
	public void ipban(String ip, String reason, String banner){
		if(sync){
			Packet p = new Packet("ipban").put("ip", ip).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
		super.ipban(ip, reason, banner);
	}
	public void tempipban(String ip, String reason, String banner, long expires){
		if(sync){
			Packet p = new Packet("tempipban").put("ip", ip).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
		super.tempipban(ip, reason, banner, expires);
	}
	public void mute(String name, String banner, String reason){
		if(sync){
			Packet p = new Packet("mute").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
		super.mute(name, banner, reason);
	}
	public void tempmute(String name, String banner, String reason, long expires){
		if(sync){
			Packet p = new Packet("tempmute").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
		super.tempmute(name, banner, reason, expires);
	}
	public void warn(String name, String reason, String banner){
		if(sync){
			Packet p = new Packet("warn").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
		super.warn(name, reason, banner);
	}
	public void clearWarnings(String name){
		if(sync){
			Packet p = new Packet("clearwarnings").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		super.clearWarnings(name);
	}
	public boolean logActual(String name, String actual){
		if(sync){
			Packet p = new Packet("setname").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		return super.logActual(name, actual);
	}
	public boolean logIP(String name, String ip){
		if(sync){
			Packet p = new Packet("setip").put("name", name).put("ip", ip);
			super.plugin.getSyncer().broadcast(p);
		}
		return super.logIP(name, ip);
	}
	public void announce(String s, boolean silent, CommandSender sender){
		if(sync){
			Packet p = new Packet("announce").put("string", s);
			if(silent){
				p.put("silent", "true");
			}
			super.plugin.getSyncer().broadcast(p);
		}
		super.announce(s, silent, sender);
	}
	
	public boolean deleteWarning(String name, Warn warn){
		if(super.deleteWarning(name, warn)){
			//Success
			if(sync){
				Packet p = new Packet("unwarn").put("name", name);
				super.plugin.getSyncer().broadcast(p);
			}
			return true;
		}
		//Failure (Invalid warning)
		return false;
	}
	
	public void ban(String name, String reason, String banner){
		if(sync){
			Packet p = new Packet("ban").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
		super.ban(name, reason, banner);
	}
	
	@Override
	public String toString(){
		return "SyncBanManager:" + super.toString();
	}
}