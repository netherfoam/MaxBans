package org.maxgamer.maxbans.database;

import org.maxgamer.maxbans.MaxBans;

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
		synchronized(db.getBuffer().queries){
			if(db.getBuffer().queries.isEmpty() == false){
				while(!db.getBuffer().queries.isEmpty()){
					try{
						BufferStatement bs = db.getBuffer().queries.remove(0);
						if(bs == null){ //NPE reported by Chalkie
							MaxBans.instance.getLogger().warning("DatabaseWatcher discovered a null query in the buffer! Skipping!");
							continue;
						}
						bs.prepareStatement(db.getConnection()).execute();
					}
					catch(Exception e){
						e.printStackTrace();
						db.getPlugin().getLogger().severe("Could not update database!");
					}
				}
			}
		}
		db.setTask(null);
		//Dont schedule the next one
		//This will be scheduled by bufferWatcher when a query is added.
	}
}
