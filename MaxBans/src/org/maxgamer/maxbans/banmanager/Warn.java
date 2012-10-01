package org.maxgamer.maxbans.banmanager;

public class Warn{
	private String reason;
	public Warn(String reason){
		this.reason = reason;
	}
	
	public String getReason(){
		return this.reason;
	}
}