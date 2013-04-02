package org.maxgamer.maxbans.commands.bridge;

import java.sql.SQLException;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;

public class SQLiteBridge implements Bridge{
	private Database db;
	public SQLiteBridge(Database db){
		this.db = db;
	}
	
	@Override
	public void export() throws SQLException {
		MaxBans.instance.getDB().copyTo(db);
	}

	@Override
	public void load() throws SQLException {
		db.copyTo(MaxBans.instance.getDB());
	}
	
}