package org.maxgamer.maxbans.database;

import java.sql.SQLException;

public class DatabaseWatcher implements Runnable{
	private Database db;
	public DatabaseWatcher(Database db){
		this.db = db;
	}

	/**
	 * What to do every time the scheduled event is called
	 * - AKA, check buffer, run queries 
	 */
	public void run() {
		while(db.getBuffer().locked){
			try {
				//1 millisecond
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//Lock it to see the size of it
		db.getBuffer().locked = true;
		
		if(db.getBuffer().queries.size() > 0){
			try{
				while(!db.getBuffer().queries.isEmpty()){
					db.getBuffer().queries.remove(0).execute();
				}
				//We can release this now
				db.getBuffer().locked = false;
			}
			catch(SQLException e){
				e.printStackTrace();
				db.getPlugin().getLogger().severe("Could not update database!");
			}
		}
		//Ensure it's released
		db.getBuffer().locked = false;
		db.setTask(null);
		//Dont schedule the next one
		//This will be scheduled by bufferWatcher when a query is added.
	}
}
