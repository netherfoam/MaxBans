package org.maxgamer.maxbans.database;

import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.ChatColor;

public class DatabaseWatcher implements Runnable{
	private Database db;
	private boolean run = true;
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
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
			db.getBuffer().queries.remove(0);
		}
		try {
			st.executeBatch();
		} catch (SQLException e3) {
			e3.printStackTrace();
			this.db.getPlugin().getLogger().severe(ChatColor.RED + "Could not execute SQL query.");
		}
		
		db.getBuffer().locked = false;
		
		//Schedule the next run of this.
		if(!this.run) return; 
		db.scheduleWatcher();
	}
	public void stop(){
		this.run = false;
	}
	public void start(){
		this.run = true;
	}
}
