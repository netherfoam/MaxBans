package org.maxgamer.maxbans.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.maxgamer.maxbans.MaxBans;

public class MySQL implements DatabaseCore{
	private MaxBans plugin;
	private String url;
	private String user;
	private String pass;
	
	private Connection connection;
	
	public MySQL(MaxBans plugin, String url, String user, String pass){
		this.plugin = plugin;
		this.url = url;
		this.user = user;
		this.pass = pass;
	}
	public MySQL(MaxBans plugin, String host, String user, String pass, String database, String port){
		this(plugin, "jdbc:mysql://"+host+":"+port+"/"+database+"?autoReconnect=true", user, pass);
	}
	
	
	/**
	 * Gets the database connection for
	 * executing queries on.
	 * @return The database connection
	 */
	public Connection getConnection(){
		try{
			//If we have a current connection, fetch it
			if(this.connection != null && !this.connection.isClosed()){
				return this.connection;
			}
			else{
				this.connection = DriverManager.getConnection(this.url, user, pass);
				return this.connection;
			}
		}
		catch(SQLException e){
			e.printStackTrace();
			plugin.getLogger().severe("Could not retrieve SQLite connection!");
		}
		return null;
	}

	/**
	 * Prepares a query for the database by fixing 's (Only works for SQLite)
	 * @param s The string to escape E.g. can't do that :\
	 * @return The escaped string. E.g. can''t do that :\
	 */
	public String escape(String s) {
		s = s.replace("\\", "\\\\");
		s = s.replace("'", "\\'");
		return s;
	}
}