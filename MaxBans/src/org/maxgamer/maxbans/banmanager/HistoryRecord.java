package org.maxgamer.maxbans.banmanager;

public class HistoryRecord{
	private String name;
	private String banner;
	private String message;
	private long created;
	
	public HistoryRecord(String name, String banner, String message){
		this(name, banner, message, System.currentTimeMillis());
	}
	public HistoryRecord(String name, String banner, String message, long created){
		this.message = message; this.created = created;
		this.banner = banner; this.name = name;
	}
	
	public String getName(){ return name; }
	public String getBanner(){ return banner; }
	public String getMessage(){ return message; }
	public long getCreated(){ return created; }
	@Override
	public String toString(){ return message; }
}