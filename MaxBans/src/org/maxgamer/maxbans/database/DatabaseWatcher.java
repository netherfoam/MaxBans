package org.maxgamer.maxbans.database;

import java.sql.PreparedStatement;

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
					BufferStatement bs = null;
					try{
						bs = db.getBuffer().queries.remove(0);
						if(bs == null){ //NPE reported by Chalkie
							MaxBans.instance.getLogger().warning("DatabaseWatcher discovered a null query in the buffer! Skipping!");
							continue;
						}
						PreparedStatement ps = bs.prepareStatement(db.getConnection());
						ps.execute();
						ps.close();
					}
					catch(Exception e){
						e.printStackTrace();
						db.getPlugin().getLogger().severe("Could not update database!");
						
						System.out.println("Creation stacktrace:");
						for(StackTraceElement st : bs.getStackTrace()){
							System.out.println(st.toString());
						}
					}
				}
			}
		}
		db.setTask(null);
		//Dont schedule the next one
		//This will be scheduled by bufferWatcher when a query is added.
	}
}
