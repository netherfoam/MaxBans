package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.util.Util;

public class Punishment{
	protected String id;
	protected String reason;
	protected String banner;
	protected long created;
	
	public Punishment(String id, String reason, String banner, long created){
		this.id = id;
		this.reason = reason;
		this.banner = banner;
		this.created = created;
	}
	
	/**
	 * The username or IP address that is punished.
	 * @return The username or IP address that is punished.
	 */
	public String getId(){
		return id;
	}
	/**
	 * The reason for the punishment
	 * @return The reason for the punishment
	 */
	public String getReason(){
		return reason;
	}
	/**
	 * The player who dealt the punishment
	 * @return The player who dealt the punishment
	 */
	public String getBanner(){
		return banner;
	}
	/**
	 * The time (from System.currentMillis()) this ban was created.
	 * @return The time (from System.currentMillis()) this ban was created.
	 */
	public long getCreated(){
		return created;
	}
	
	@Override
	public String toString(){
		String str = getClass().getSimpleName() + " ID:" + id + (this.reason != null && !this.reason.isEmpty() ? ", R:" + reason : "") + ", B:" + banner + ", C:" + created;
		if(this instanceof Temporary){
			Temporary t = (Temporary) this;
			str += ", E:" + Util.getTimeUntil(t.getExpires());
		}
		return str;
	}
}