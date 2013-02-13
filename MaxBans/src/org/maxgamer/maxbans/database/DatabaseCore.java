package org.maxgamer.maxbans.database;

import java.sql.Connection;

public interface DatabaseCore{
	/** Returns an active connection to the database */
	public Connection getConnection();
}