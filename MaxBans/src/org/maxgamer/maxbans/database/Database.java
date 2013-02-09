package org.maxgamer.maxbans.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.util.Util;

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
	private boolean readOnly;
	
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
		this.dbCore = new MySQL(plugin, host, user, pass, dbName, port);
	}
	private Database(MaxBans plugin){
		this.plugin = plugin;
		this.buffer = new Buffer(this);
		this.dbw = new DatabaseWatcher(this);
	}
	
	public void setReadOnly(boolean readOnly){ this.readOnly = readOnly; }
	public boolean isReadOnly(){ return this.readOnly; }
	public DatabaseCore getCore(){ return this.dbCore; }
	
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
	 * Copies the contents of this database into the given database. 
	 * Does not delete the contents of this database, or change any
	 * settings. The plugin will STILL USE *THIS* database, and not
	 * use the other.  This may take a long time, and will print out
	 * progress reports to System.out
	 * @param db The database to copy data to
	 * @throws SQLException if an error occurs.
	 */
	public void copyTo(Database db) throws SQLException{
		ResultSet rs = plugin.getDB().getConnection().getMetaData().getTables(null, null, "%", null);
		List<String> tables = new LinkedList<String>();
		while(rs.next()){
			tables.add(rs.getString("TABLE_NAME"));
		}
		rs.close();
		
		plugin.getDB().getDatabaseWatcher().run(); //Flush the current query set.
		db.createTables();
		
		//For each table
		for(String table : tables){
			if(table.toLowerCase().startsWith("sqlite_autoindex_")) continue;
			System.out.println("Copying " + table);
			//Wipe the old records
			db.getConnection().prepareStatement("DELETE FROM " + table).execute();
			
			//Fetch all the data from the existing database
			rs = plugin.getDB().getConnection().prepareStatement("SELECT * FROM " + table).executeQuery();
			
			int n = 0;
			
			//Build the query
			String query = "INSERT INTO " + table + " VALUES (";
			//Append another placeholder for the value
			query += "?";
			for(int i = 2; i <= rs.getMetaData().getColumnCount(); i++){
				//Add the rest of the placeholders and values.  This is so we have (?, ?, ?) and not (?, ?, ?, ).
				query += ", ?";
			}
			//End the query
			query += ")";
			
			PreparedStatement ps = db.getConnection().prepareStatement(query);
			while(rs.next()){
				n++;
				
				for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
					ps.setObject(i, rs.getObject(i));
				}
				
				ps.addBatch();
				
				if(n % 100 == 0){
					ps.executeBatch();
					System.out.println(n + " records copied...");
				}
			}
			ps.executeBatch();
			//Close the resultset of that table
			rs.close();
		}
		//Success!
		db.getConnection().close();
		this.getConnection().close();
	}
	
	/**
	 * Just like java.sql.PreparedStatement, except this query will be executed later in a different thread.
	 * @param query The query to execute
	 * @param objs The Strings to replace ?'s with in the query supplied above
	 */
	public void execute(String query, Object...objs){
		if(this.isReadOnly()) return; //Read only.
		BufferStatement bs = new BufferStatement(query, objs);
		this.getBuffer().add(bs);
	}
	
	/**
	 * Returns true if the table exists
	 * @param table The table to check for
	 * @return True if the table is found
	 */
	public boolean hasTable(String table) throws SQLException{
		try{
			ResultSet rs = getConnection().getMetaData().getTables(null, null, "%", null);
			while(rs.next()){
				if(table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) return true;
			}
			rs.close();
		}
		catch(NullPointerException e){
			throw new SQLException("Invalid connection");
		}
		return false;
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
	public void createTables() throws SQLException{
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
		if(!this.hasTable("proxys")){
			this.createProxysTable();
		}
		if(!this.hasTable("history")){
			this.createHistoryTable();
		}
		if(!this.hasTable("players")){
			this.createPlayersTable();
			ResultSet rs = this.getConnection().prepareStatement("SELECT * FROM iphistory").executeQuery();
			List<String> names = new ArrayList<String>();
			while(rs.next()){
				names.add(rs.getString("name"));
			}
			rs.close();
			
			if(names.isEmpty() == false){
				System.out.println("Created players table. Now converting old player list. Size: "+names.size() + ", please wait :)");
				
				int n = 0;
				PreparedStatement ps = this.getConnection().prepareStatement("INSERT INTO players (name, actual) VALUES (?, ?)");
				for(String name : names){
					n++;
					
					ps.setString(1, name);
					ps.setString(2, name);
					ps.addBatch();
					
					if(n % 100 == 0){
						long start = System.currentTimeMillis();
						ps.executeBatch();
						long end = System.currentTimeMillis();
						
						long remaining = (names.size() - n)/100 *  (end - start);
						System.out.println(n + " records copied... Remaining: " + Util.getTime(remaining));
					}
				}
				ps.executeBatch();
				//Close the resultset of that table
				rs.close();
			}
		}
		if(!this.hasColumn("warnings", "expires")){
			try {
				this.getConnection().prepareStatement(" ALTER TABLE warnings ADD expires long").execute();
			} catch (SQLException e) {} //Already has expires column. Just no record of warnings yet.
		}
	}
	
	public void createHistoryTable(){
		String query = "CREATE TABLE history (created BIGINT NOT NULL, message TEXT(100));";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create history table.");
		}
	}
	
	public void createPlayersTable(){
		String query = "CREATE TABLE players (name TEXT(30) NOT NULL, actual TEXT(30) NOT NULL);";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create players table.");
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
	 * Creates the proxys table
	 */
	public void createProxysTable(){
		String query = "CREATE TABLE proxys (ip TEXT(30) NOT NULL, status TEXT(30), created BIGINT NOT NULL)";
		try {
			Statement st = this.getConnection().createStatement();
			st.execute(query);
		} catch (SQLException e) {
			e.printStackTrace();
			this.plugin.getLogger().severe(ChatColor.RED + "Could not create proxys table.");
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
}
