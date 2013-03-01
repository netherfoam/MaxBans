package org.maxgamer.maxbans.database;

import java.util.ArrayList;

import org.bukkit.Bukkit;

public class Buffer {
	private Database db;
	//public boolean locked = false;
	
	public ArrayList<BufferStatement> queries = new ArrayList<BufferStatement>(3);
	
	public Buffer(Database db){
		this.db = db;
	}
	
	/**
	 * Adds a query to the buffer
	 * @param q The query to add.  This should be sanitized beforehand.
	 */
	public void add(BufferStatement bs){
		Bukkit.getScheduler().runTaskAsynchronously(db.getPlugin(), new BufferWatcher(db, this, bs));
	}
}
