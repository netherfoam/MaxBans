package org.maxgamer.maxbans.database;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class Buffer {
	private Database db;
	public boolean locked = false;
	
	public List<String> queries = new ArrayList<String>(3);
	
	public Buffer(Database db){
		this.db = db;
	}
	
	/**
	 * Adds a query to the buffer
	 * @param q The query to add.  This should be sanitized beforehand.
	 */
	public void addString(String q){
		Bukkit.getScheduler().scheduleAsyncDelayedTask(db.getPlugin(), new BufferWatcher(this, q));
	}
}
