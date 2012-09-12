package org.maxgamer.maxbans.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;
import org.maxgamer.maxbans.MaxBans;

public class Database {
	private Buffer buffer;
	private MaxBans plugin;
	private File dbFile;
	
	public Database(MaxBans plugin, File file){
		this.plugin = plugin;
		this.dbFile = file;
		this.buffer = new Buffer(this);
	}

	public Plugin getPlugin() {
		return this.plugin;
	}
	public Buffer getBuffer(){
		return this.buffer;
	}

	/**
	 * Gets the database connection for
	 * executing queries on.
	 * @return The database connection
	 */
	public Connection getConnection(){
		if(!this.dbFile.exists()){
			plugin.getLogger().info("CRITICAL: Database does not exist");
			try {
				this.dbFile.createNewFile();
				Class.forName("org.sqlite.JDBC");
				Connection dbCon = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
				return dbCon;
			} 
			catch (IOException e) {
				e.printStackTrace();
				plugin.getLogger().info("Could not create file " + this.dbFile.toString());
			} 
			catch (ClassNotFoundException e) {
				e.printStackTrace();
				plugin.getLogger().info("You need the SQLite JBDC library.  Put it in MinecraftServer/lib folder.");
			} catch (SQLException e) {
				e.printStackTrace();
				plugin.getLogger().info("SQLite exception on initialize " + e);
			}
		}
		try{
			Class.forName("org.sqlite.JDBC");
			return DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
		}
		catch(SQLException e){
			e.printStackTrace();
			plugin.getLogger().info("SQLite exception on initialize.");
		}
		catch(ClassNotFoundException e){
			e.printStackTrace();
			plugin.getLogger().info("SQLite library not found, was it removed?");
		}
		return null;
	}
}
