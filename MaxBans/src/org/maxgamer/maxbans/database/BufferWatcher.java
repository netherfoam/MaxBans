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
		while(buffer.locked){
			try {
				//1 millisecond
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		buffer.locked = true;
		buffer.queries.add(bs);
		buffer.locked = false;
		
		if(db.getTask() == null){
			db.scheduleWatcher();
		}
	}
}