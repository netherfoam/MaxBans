package org.maxgamer.maxbans.database;

import java.sql.PreparedStatement;

/**
 * This class is used for running queries in another thread on the database.
 * This class cannot be used to read data.
 */
public class BufferWatcher implements Runnable{
	private Buffer buffer;
	private PreparedStatement ps;
	private Database db;
	
	public BufferWatcher(Database db, Buffer buffer, PreparedStatement ps){
		this.buffer = buffer;
		this.ps = ps;
		this.db = db;
	}
	public void run() {
		while(buffer.locked){
			try {
				//1 millisecond
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		buffer.locked = true;
		buffer.queries.add(ps);
		buffer.locked = false;
		
		if(db.getTask() == null){
			db.scheduleWatcher();
		}
	}
}