package org.maxgamer.maxbans.database;

import java.sql.SQLException;
import java.sql.Statement;

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
		Statement st = null;
		try {
			st = db.getConnection().createStatement();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		while(db.getBuffer().locked){
			//Nothing
		}
		db.getBuffer().locked = true;
		
		while(db.getBuffer().queries.size() > 0){
			try {
				st.addBatch(db.getBuffer().queries.get(0));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.getBuffer().queries.remove(0);
		}
		
		db.getBuffer().locked = false;
	}
}
