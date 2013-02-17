package org.maxgamer.maxbans.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeSet;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Temporary;

public class Ranger{
	private TreeSet<RangeBan> banned = new TreeSet<RangeBan>();
	private MaxBans plugin;
	
	public Ranger(MaxBans plugin){
		this.plugin = plugin;
		
		String query = "SELECT * FROM rangebans";
		
		try{
			ResultSet rs = plugin.getDB().getConnection().prepareStatement(query).executeQuery();
			while(rs.next()){
				String banner = rs.getString("banner");
				String reason = rs.getString("reason");
				IPAddress start = new IPAddress(rs.getString("start"));
				IPAddress end = new IPAddress(rs.getString("end"));
				long created = rs.getLong("created");
				long expires = rs.getLong("created");
				
				RangeBan rb;
				if(expires == 0){
					rb = new TempRangeBan(banner, reason, created, expires, start, end);
				}
				else{
					rb = new RangeBan(banner, reason, created, start, end);
				}
				
				banned.add(rb);
			}
		}
		catch(SQLException e){
			e.printStackTrace();
			plugin.getLogger().warning("Could not load rangebans!");
		}
	}
	
	/**
	 * Returns true if the given IP is banned
	 * @param ip The IPAddress to check
	 * @return true if the given IP is banned
	 */
	public boolean isBanned(IPAddress ip){
		return getBan(ip) != null;
	}
	
	/**
	 * Fetches the RangeBan for the given IP address
	 * @param ip The IP Address
	 * @return The RangeBan. This will always contain
	 * the given IP address, and will never be expired.
	 * If either of these two conditions are not satisfied,
	 * then this method returns null.
	 */
	public RangeBan getBan(IPAddress ip){
		RangeBan dummy = new RangeBan("dummy", "n/a", System.currentTimeMillis(), ip, ip);
		RangeBan rb = banned.floor(dummy);
		if(rb == null) return null;
		if(rb.contains(ip)){
			if(rb instanceof Temporary){
				if(((Temporary) rb).hasExpired()){
					unban(rb);
					return null; //Ban is dead.
				}
			}
			
			return rb;
		}
		return null;
	}
	
	/**
	 * Bans the given range of IPs so that isBanned(rb.getEnd() through to rb.getStart()) is banned.
	 * @param rb The RangeBan to apply
	 * @return The RangeBan which is in the way of doing this. If this method returns null, then it has succeeded
	 */
	public RangeBan ban(RangeBan rb){
		RangeBan previous = banned.floor(rb);
		if(previous != null){
			if(previous.overlaps(rb)){
				return previous;
			}
		}
		previous = banned.ceiling(rb);
		if(previous != null){
			if(previous.overlaps(rb)){
				return previous;
			}
		}
		
		banned.add(rb);
		long expires = 0;
		if(rb instanceof Temporary){
			expires = ((Temporary) rb).getExpires();
		}
		plugin.getDB().execute("INSERT INTO rangebans (banner, reason, start, end, created, expires) VALUES (?, ?, ?, ?, ?, ?)", rb.getBanner(), rb.getReason(), rb.getStart(), rb.getEnd(), rb.getCreated(), expires);
		return null;
	}
	
	/**
	 * Deletes the given rangeban from memory and the database.
	 * @param rb The rangeban to lift
	 */
	public void unban(RangeBan rb){
		if(banned.contains(rb)){
			banned.remove(rb);
			plugin.getDB().execute("DELETE FROM rangebans WHERE start = ? AND end = ?", rb.getStart(), rb.getEnd());
		}
	}
}