package org.maxgamer.maxbans.banmanager;

public class HistoryRecord{
	private String message;
	private long created;
	
	public HistoryRecord(String message){
		this(message, System.currentTimeMillis());
	}
	public HistoryRecord(String message, long created){
		this.message = message; this.created = created;
	}
	
	public String getMessage(){ return message; }
	public long getCreated(){ return created; }
	@Override
	public String toString(){ return message; }
}