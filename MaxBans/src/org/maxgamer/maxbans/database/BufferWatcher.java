package org.maxgamer.maxbans.database;

/**
 * This class is used for running queries in another thread on the database.
 * This class cannot be used to read data.
 */
public class BufferWatcher implements Runnable{
	private Buffer buffer;
	private BufferStatement bs;
	private Database db;
	
	public BufferWatcher(Database db, Buffer buffer, BufferStatement bs){
		this.buffer = buffer;
		this.bs = bs;
		this.db = db;
	}
	public void run() {
		synchronized(buffer.queries){
			buffer.queries.add(bs);
		}
		if(db.getTask() != null){
			db.scheduleWatcher();
		}
	}
}