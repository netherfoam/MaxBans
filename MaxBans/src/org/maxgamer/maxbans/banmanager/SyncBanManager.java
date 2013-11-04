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
		super.addHistory(name, banner, message);
		if(sync){
			Packet p = new Packet("addhistory").put("string", message).put("banner", banner).put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	
	public RangeBan ban(RangeBan rb){
		RangeBan b = super.ban(rb);
		if(sync){
			Packet p = new Packet("rangeban").put("reason", rb.getReason()).put("start", rb.getStart().toString()).put("end", rb.getEnd().toString()).put("banner", rb.getBanner()).put("created", rb.getCreated());
			super.plugin.getSyncer().broadcast(p);
		}
		return b;
	}
	public void unban(RangeBan rb){
		super.unban(rb);
		if(sync){
			Packet p = new Packet("rangeban").put("start", rb.getStart().toString()).put("end", rb.getEnd().toString());
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void setWhitelisted(String name, boolean white){
		super.setWhitelisted(name, white);
		if(sync){
			Packet p = new Packet("whitelist").put("name", name);
			if(white){
				p.put("white", "");
			}
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void kick(String user, String msg){
		super.kick(user, msg);
		if(sync){
			Packet p = new Packet("kick").put("name", user).put("reason", msg);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void kickIP(String ip, String msg){
		super.kickIP(ip, msg);
		if(sync){
			Packet p = new Packet("kickip").put("ip", ip).put("reason", msg);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public boolean setImmunity(String user, boolean immune){
		boolean change = super.setImmunity(user, immune);
		if(sync){
			Packet p = new Packet("setimmunity").put("name", user);
			if(immune){
				p.put("immune", "");
			}
			super.plugin.getSyncer().broadcast(p);
		}
		return change;
	}
	
	public void unban(String name){
		super.unban(name);
		if(sync){
			Packet p = new Packet("unban").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void unbanip(String ip){
		super.unbanip(ip);
		if(sync){
			Packet p = new Packet("unbanip").put("ip", ip);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void unmute(String name){
		super.unmute(name);
		if(sync){
			Packet p = new Packet("unmute").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void tempban(String name, String reason, String banner, long expires){
		super.tempban(name, reason, banner, expires);
		if(sync){
			Packet p = new Packet("tempban").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void ipban(String ip, String reason, String banner){
		super.ipban(ip, reason, banner);
		if(sync){
			Packet p = new Packet("ipban").put("ip", ip).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void tempipban(String ip, String reason, String banner, long expires){
		super.tempipban(ip, reason, banner, expires);
		if(sync){
			Packet p = new Packet("tempipban").put("ip", ip).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void mute(String name, String banner, String reason){
		super.mute(name, banner, reason);
		if(sync){
			Packet p = new Packet("mute").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void tempmute(String name, String banner, String reason, long expires){
		super.tempmute(name, banner, reason, expires);
		if(sync){
			Packet p = new Packet("tempmute").put("name", name).put("reason", reason).put("banner", banner).put("expires", expires);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void warn(String name, String reason, String banner){
		super.warn(name, reason, banner);
		if(sync){
			Packet p = new Packet("warn").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public void clearWarnings(String name){
		super.clearWarnings(name);
		if(sync){
			Packet p = new Packet("clearwarnings").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	public boolean logActual(String name, String actual){
		boolean change = super.logActual(name, actual);
		if(sync){
			Packet p = new Packet("setname").put("name", name);
			super.plugin.getSyncer().broadcast(p);
		}
		return change;
	}
	public boolean logIP(String name, String ip){
		boolean change = super.logIP(name, ip);
		if(sync){
			Packet p = new Packet("setip").put("name", name).put("ip", ip);
			super.plugin.getSyncer().broadcast(p);
		}
		return change;
	}
	public void announce(String s, boolean silent, CommandSender sender){
		super.announce(s, silent, sender);
		if(sync){
			Packet p = new Packet("announce").put("string", s);
			if(silent){
				p.put("silent", "true");
			}
			super.plugin.getSyncer().broadcast(p);
		}
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
		super.ban(name, reason, banner);
		if(sync){
			Packet p = new Packet("ban").put("name", name).put("reason", reason).put("banner", banner);
			super.plugin.getSyncer().broadcast(p);
		}
	}
	
	@Override
	public String toString(){
		return "SyncBanManager:" + super.toString();
	}
}