package org.maxgamer.maxbans.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.maxgamer.maxbans.MaxBans;

public class SQLite implements DatabaseCore{
	private Connection connection;
	private File dbFile;
	private MaxBans plugin;
	
	public SQLite(MaxBans plugin, File dbFile){
		this.plugin = plugin;
		this.dbFile = dbFile;
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
		}
		catch(SQLException e){
			e.printStackTrace();
			plugin.getLogger().severe("Could not retrieve SQLite connection!");
		}
		
		if(this.dbFile.exists()){
			//So we need a new connection
			try{
				Class.forName("org.sqlite.JDBC");
				this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
				return this.connection;
			}
			catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		else{
			//So we need a new file too.
			try {
				//Create the file
				this.dbFile.createNewFile();
				//Now we won't need a new file, just a connection.
				//This will return that new connection.
				return this.getConnection();
			} catch (IOException e) {
				e.printStackTrace();
				plugin.getLogger().severe("Could not create database file!");
				return null;
			}
		}
	}

	/**
	 * Prepares a query for the database by fixing 's (Only works for SQLite)
	 * @param s The string to escape E.g. can't do that :\
	 * @return The escaped string. E.g. can''t do that :\
	 */
	public String escape(String s) {
		return s.replace("'", "''");
	}
}