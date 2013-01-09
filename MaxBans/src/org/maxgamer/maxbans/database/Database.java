package org.maxgamer.maxbans.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.maxbans.MaxBans;

/**
 * Suggested database format:
 * Table: Bans
 * 	Name	Reason	Banner	Time	Expires	
 * 	Chuck	Spam	Azgod	143134	143956
 * 
 * Where,
 * 		Name (Lowercase) is the name of the player to ban,
 * 		Reason is the reason of the ban,
 * 		Banner is the admin (Lowercase) who banned them,
 * 		Time is the time the ban was created,
 * 		Expires is the time when the ban runs out.
 * 		If expires = 0, then it never expires.
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
 * 
 * ====>	New	   <====
 * Table: IPHistory
 * Name		IP
 * Azgod	127.0.0.1
 * Azgod	192.168.2.1
 * Frizire	127.0.0.1
 * Daxter	192.168.2.15
 * 
 * Where,
 * 		Name + IP combo is unique
 * 		Name is the lowercase name
 * 		IP is the ip theyve been recorded on.
 *
 * Table: Mutes
 * Name		Muter	Time	Expires
 * Catty	Frizire	204234	0
 * 
 * Where,
 * 		Name is the muted player. (Lowercase)
 * 		Muter is the guy who muted them (Like banner)
 * 		Time is the time they were muted
 * 		Expires is the time that the mute expires. (0 = never)
 * 
 * Table: Warnings
 * Name		Reason			Banner
 * adam126	Stupid noob		darekfive
 * 
 * Where,
 * 		Name is the warned player, lowercase
 * 		Reason is the reason for the warning
 * 		Banner is the admin who warned them
 */
public class Database {
	private DatabaseCore dbCore;
	
	private Buffer buffer;
	private MaxBans plugin;
	private DatabaseWatcher dbw;

	private BukkitTask task;
	
	/**
	 * Creates a new database connection, using SQLite flatfile.
	 * The Database object handles file creation.
	 * @param plugin The MaxBans plugin instance
	 * @param file The File object to use for SQL storage.
	 */
	public Database(MaxBans plugin, File file){
		this(plugin);
		this.dbCore = new SQLite(plugin, file);
	}
	/**
	 * Still experimental.
	 * @param plugin The MaxBans plugin
	 * @param host The host address (Eg 127.0.0.1 or maxgamer.org)
	 * @param dbName The name of the MySQL database (Eg maxbans). This object does NOT handle creating the database.
	 * @param user The username to connect as - E.g. root
	 * @param pass The password for the given user
	 * @param port The port to connect on.
	 */
	public Database(MaxBans plugin, String host, String dbName, String user, String pass, String port){
		this(plugin);
		String url = "jdbc:mysql://"+host+":"+port+"/"+dbName;
		this.dbCore = new MySQL(plugin, url, user, pass);
	}
	private Database(MaxBans plugin){
		this.plugin = plugin;
		this.buffer = new Buffer(this);
		this.dbw = new DatabaseWatcher(this);
	}
	
	/**
	 * Reschedules the db watcher
	 */
	public void scheduleWatcher(){
		this.task = Bukkit.getScheduler().runTaskLater(plugin, this.dbw, 300);
	}
	
	public BukkitTask getTask(){
		return task;
	}
	public void setTask(BukkitTask task){
		this.task = task; 
	}
	
	public DatabaseWatcher getDatabaseWatcher(){
		return this.dbw;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}
	public Buffer getBuffer(){
		return this.buffer;
	}
	
	/**
	 * Just like java.sql.PreparedStatement, except this query will be executed later in a different thread.
	 * @param query The query to execute
	 * @param objs The Strings to replace ?'s with in the query supplied above
	 */
	public void execute(String query, Object...objs){
		BufferStatement bs = new BufferStatement(query, objs);
		this.getBuffer().add(bs);
	}
	
	/**
	 * Returns true if the table exists
	 * @param table The table to check for
	 * @return True if the table is found
	 */
	public boolean hasTable(String table){
		String query = "SELECT * FROM " + table + " LIMIT 0,1";
		try {
			PreparedStatement ps = this.getConnection().prepareStatement(query);
			
			ps.executeQuery();
			
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public boolean hasColumn(String table, String column){
		String query = "SELECT * FROM " + table + " LIMIT 0,1";
		try{
			PreparedStatement ps = this.getConnection().prepareStatement(query);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()){
				rs.getString(column); //Throws an exception if it can't find that column
				return true;
			}
		}
		catch(SQLException e){
			return false;
		}
		return false; //Uh, wtf.
	}
	
	/**
	 * Creates the database tables (bans ipbans mutes iphistory and warnings)
	 */
	public void createTables(){
		//Creates the database tables
		if(!this.hasTable("bans")){
			this.createBanTable();
		}
		if(!this.hasTable("ipbans")){
			this.createIPBanTable();
		}
		if(!this.hasTable("mutes")){
			this.createMuteTable();
		}
		if(!this.hasTable("iphistory")){
			this.createIPHistoryTable();
		}
		if(!this.hasTable("warnings")){
			this.createWarningsTable();
		}
		else if(!this.hasColumn("warnings", "expires")){
			try {
				this.getConnection().prepareStatement(" ALTER TABLE warnings ADD expires long").execute();
			} catch (SQLException e) {
				System.out.println("WTF, couldnt add column!");
			}
		}
	}
	
	/**
	 * Creates the bans table
	 */
	public void createBanTable(){
		String query = "CREATE TABLE bans ( name  TEXT(30) NOT NULL, reason  TEXT(100), banner  TEXT(30), time  BIGINT NOT NULL DEFAULT 0, expires  BIGINT NOT NULL DEFAULT 0 );";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create bans table.");
		}
	}
	
	/**
	 * Creates the IPBan table
	 */
	public void createIPBanTable(){
		String query = "CREATE TABLE ipbans ( ip  TEXT(20) NOT NULL, reason  TEXT(100), banner  TEXT(30), time  BIGINT NOT NULL DEFAULT 0, expires  BIGINT NOT NULL DEFAULT 0 );";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create ipbans table.");
		}
	}
	
	/**
	 * Creates the mutes table
	 */
	public void createMuteTable(){
		String query = "CREATE TABLE mutes ( name  TEXT(30) NOT NULL, muter  TEXT(30), time  BIGINT DEFAULT 0, expires  BIGINT DEFAULT 0 );";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create mutes table.");
		}
	}
	
	/**
	 * Creates the iphistory table
	 */
	public void createIPHistoryTable(){
		String query = "CREATE TABLE iphistory ( name  TEXT(30) NOT NULL, ip  TEXT(20) NOT NULL);";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create iphistory table.");
		}
	}
	
	/**
	 * Creates the warnings table
	 */
	public void createWarningsTable(){
		String query = "CREATE TABLE warnings (name TEXT(30) NOT NULL, reason TEXT(100) NOT NULL, banner TEXT(30) NOT NULL, expires BIGINT(30));";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create iphistory table.");
		}
	}

	/**
	 * Gets the database connection for
	 * executing queries on.
	 * @return The database connection
	 */
	public Connection getConnection(){
		return this.dbCore.getConnection();
	}
	
	public String escape(String s){
		return this.dbCore.escape(s);
	}
	
}
