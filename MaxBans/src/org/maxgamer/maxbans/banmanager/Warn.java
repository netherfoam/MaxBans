package org.maxgamer.maxbans.banmanager;

public class Warn{
	private String reason;
	private String banner;
	public Warn(String reason, String banner){
		this.reason = reason;
		this.banner = banner;
	}
	
	public String getReason(){
		return this.reason;
	}
	
	public String getBanner(){
		return this.banner;
	}
}