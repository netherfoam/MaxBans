package org.maxgamer.maxbans.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.maxgamer.maxbans.MaxBans;

public class MySQL implements DatabaseCore{
	private MaxBans plugin;
	private String url;
	/** The connection properties... user, pass, autoReconnect.. */
	private Properties info;
	/** The actual connection... possibly expired. */
	private Connection connection;
	
	public MySQL(MaxBans plugin, String host, String user, String pass, String database, String port){
		info = new Properties();
		info.put("autoReconnect", "true");
		info.put("user", user);
		info.put("password", pass);
		info.put("useUnicode", "true");
		info.put("characterEncoding", "utf8");
		this.url = "jdbc:mysql://"+host+":"+port+"/"+database;
		this.plugin = plugin;
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
				//this.connection = DriverManager.getConnection(this.url, user, pass);
				this.connection = DriverManager.getConnection(this.url, info);
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