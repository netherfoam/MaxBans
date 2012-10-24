package org.maxgamer.maxbans.database;

/**
 * This class is used for running queries in another thread on the database.
 * This class cannot be used to read data.
 */
public class BufferWatcher implements Runnable{
	private Buffer buffer;
	private String query;
	private Database db;
	
	public BufferWatcher(Database db, Buffer buffer, String query){
		this.buffer = buffer;
		this.query = query;
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
		buffer.queries.add(query);
		buffer.locked = false;
		
		if(db.getWatcherId() == 0){
			db.scheduleWatcher();
		}
	}
}