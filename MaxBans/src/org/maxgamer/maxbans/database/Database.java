package org.maxgamer.maxbans.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;
import org.maxgamer.maxbans.MaxBans;

/**
 * Suggested database format:
 * Table: Bans
 * 	Player	Reason	Banner	Time	Expires	
 * 	Chuck	Spam	Azgod	143134	143956
 * 
 * Where,
 * 		Player (Lowercase) is the name of the player to ban,
 * 		Reason is the reason of the ban,
 * 		Banner is the admin (Lowercase) who banned them,
 * 		Time is the time the ban was created,
 * 		Expires is the time when the ban runs out.
 * 
 * Table: IPBans
 * 	IP		Reason	Banner	Time	Expires
 * 	127.*	Spam	Azgod	143134	143956
 * 
 * Where,
 * 		IP is the players IP,
 * 		Reason is the ban reason,
 * 		Banner (Lowercase) is the admin who banned them,
 * 		Time is the time the ban was created,
 * 		Expires is the time the ban will expire (0 = Never)
 * 
 * Table: IPHistory
 * Player	Time	IP			Count
 * Azgod	141134	127.0.0.1	1
 * Azgod	143234	192.168.2.1	15
 * Darek	143144	127.0.0.1	3
 * Daxter	133134	127.0.0.1	8
 * 
 * Where,
 * 		Player (Lowercase) is player name,
 * 		Time is last login on that IP,
 * 		IP is the ip address,
 * 		Count is the number of times they've logged in from that IP.
 *
 */
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
