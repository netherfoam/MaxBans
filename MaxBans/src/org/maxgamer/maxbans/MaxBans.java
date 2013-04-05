package org.maxgamer.maxbans;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.commands.*;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.database.Database.ConnectionException;
import org.maxgamer.maxbans.database.DatabaseCore;
import org.maxgamer.maxbans.database.MySQLCore;
import org.maxgamer.maxbans.database.SQLiteCore;
import org.maxgamer.maxbans.listeners.*;
import org.maxgamer.maxbans.sync.SyncServer;
import org.maxgamer.maxbans.sync.Syncer;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Metrics;
import org.maxgamer.maxbans.util.Metrics.Graph;

/**
 * The MaxBans plugin.<br/>
 * <br/>
 * The goal of this plugin is to be a lightweight and bug-free<br/>
 * banning plugin.  It offers a variety of tools to help Bukkit<br/>
 * server staff to manage administration tasks such as banning,<br/>
 * kicking, muting, all temporary variants, preventing proxies,<br/>
 * and locking down the server.<br/>
 * <br/>
 * <b>Server Website</b> 	http://maxgamer.org/<br/></a>
 * <b>Primary Server IP</b> play.maxgamer.org<br/>
 * <b>Source code</b> 		http://github.com/netherfoam/MaxBans/<br/>
 * <b>Compiled plugin</b>   http://dev.bukkit.org/server-mods/maxbans/<br/><br/>
 * @author Netherfoam, Darekfive<br/><br/>
 */
public class MaxBans extends JavaPlugin{
    private BanManager banManager;
    private Syncer syncer;
    private SyncServer syncServer;
            
    private JoinListener joinListener;
    private HeroChatListener herochatListener; 
    private ChatListener chatListener;
    private ChatCommandListener chatCommandListener;
    
    private Database db;
    private Metrics metrics;
    
    /** Should we filter players names onJoin? */
    public boolean filter_names;
    
    /** The one plugin instance */
    public static MaxBans instance;
        
	public void onEnable(){
		instance = this;
		
		/* Generates files for the first run */
		if(!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		File configFile = new File(this.getDataFolder(), "config.yml");
		
		if(!configFile.exists()){
			//Saves config.yml from inside the plugin
			//into the plugins directory folder
			this.saveResource("config.yml", false);
		}
		
		/*
		 * Reloads the config from disk.
		 * Normally this is done before onEnable()
		 * anyway, but, if we do /plugman reload MaxBans,
		 * it doesnt.  This makes it friendlier.
		 */
		this.reloadConfig();
		
		int result = UpdateCheck.compareVersion(getConfig().getString("version"), this.getDescription().getVersion());
		
		this.getConfig().options().copyDefaults();
		if(result == -1){ //Config needs updating
			getLogger().info("Updating config!");
			this.saveConfig();
		}
		
		this.filter_names = getConfig().getBoolean("filter-names");
		
		Formatter.load(this);
		
		ConfigurationSection dbConfig = getConfig().getConfigurationSection("database");
		
		DatabaseCore dbCore;
		if(getConfig().getBoolean("database.mysql", false)){
			getLogger().info("Using MySQL");
			String user = dbConfig.getString("user");
			String pass = dbConfig.getString("pass");
			String host = dbConfig.getString("host");
			String name = dbConfig.getString("name");
			String port = dbConfig.getString("port");
			//db = new Database(this, host, name, user, pass, port);
			dbCore = new MySQLCore(host, user, pass, name, port);
		}
		else{
			getLogger().info("Using SQLite");
			//The database for bans
			dbCore = new SQLiteCore(new File(this.getDataFolder(), "bans.db"));
		}
		final boolean readOnly = dbConfig.getBoolean("read-only", false);
		
		//Read-only hack
		try {
			this.db = new Database(dbCore){
				@Override
				public void execute(String query, Object... objs){
					if(readOnly) return;
					else super.execute(query, objs);
				}
			};
		} catch (ConnectionException e1) {
			e1.printStackTrace();
			System.out.println("Failed to create connection to database. Disabling MaxBans :(");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		//BanManager
		banManager = new BanManager(this);
		
		ConfigurationSection syncConfig = this.getConfig().getConfigurationSection("sync");
		if(syncConfig.getBoolean("use", false)){
			getLogger().info("Using Sync.");
			final String host = syncConfig.getString("host");
			final int port = syncConfig.getInt("port");
			final String pass = syncConfig.getString("pass");
			
			if(syncConfig.getBoolean("server", false)){
				try{
					this.syncServer = new SyncServer(port, pass);
				}
				catch(IOException e){
					e.printStackTrace();
					getLogger().info("Could not start sync server!");
				}
				catch(NoSuchAlgorithmException e){
					e.printStackTrace();
					getLogger().info("Could not encrypt SyncServer password!");
				}
			}
			
			try {
				syncer = new Syncer(host, port, pass);
				syncer.start();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				getLogger().info("Could not encrypt SyncServer password!");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				getLogger().info("Could not encrypt SyncServer password!");
			}
		}
		
		
		//Commands
		registerCommands();
		
		//Listeners for chat (mute) and join (Ban)
		if(Bukkit.getPluginManager().getPlugin("Herochat") != null){
			this.getLogger().info("Found Herochat... Hooking!");
			this.herochatListener = new HeroChatListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(this.herochatListener, this);
		}
		else{
			this.chatListener = new ChatListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(this.chatListener, this);
		}
		this.joinListener = new JoinListener();
		this.chatCommandListener = new ChatCommandListener();
        
        Bukkit.getServer().getPluginManager().registerEvents(this.joinListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.chatCommandListener, this);
        
        startMetrics();
        
        if(getConfig().getBoolean("update-check", true)){
	        Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable(){
	        	public void run(){
	        		if(UpdateCheck.hasUpdate()){
	        			getLogger().info("There is a new update for MaxBans.");
	        			getLogger().info("Please visit http://dev.bukkit.org/server-mods/MaxBans to download it.");
	        		}
	        	}
        	}, 0);
        }
    }
	
	public void onDisable(){
		this.getLogger().info("Disabling Maxbans...");
		
		if(syncer != null){
			syncer.stopReconnect();
			syncer.stop();
			syncer = null; //Required when reloading, if sync.use changes to false
		}
		
		if(syncServer != null){
			syncServer.stop();
			syncServer = null; //Required when reloading, if sync.server changes to false
		}
		
		this.getLogger().info("Clearing buffer...");
		
		this.db.close();
		this.getLogger().info("Cleared buffer...");
		instance = null;
	}
	/**
	 * Returns the ban manager for banning and checking bans and mutes.
	 * @return the ban manager for banning and checking bans and mutes.
	 */
    public BanManager getBanManager() {
        return banManager;
    }
        
    /**
     * Returns the raw database for storing data and loading into the cache.
     * @return the raw database for storing data and loading into the cache.
     */
    public Database getDB(){
    	return db;
    }
    
    /**
     * Creates a new instance of each command and registers it
     */
    public void registerCommands(){
    	//Instances
    	new BanCommand();
		new IPBanCommand();
		new MuteCommand();
		
		new TempBanCommand();
		new TempIPBanCommand();
		new TempMuteCommand();
		
		new UnbanCommand();
		new UnMuteCommand();
		
		new CheckIPCommand();
		new CheckBanCommand();
		new DupeIPCommand();
		
		new WarnCommand();
		new ClearWarningsCommand();
		
		new LockdownCommand();
		new KickCommand();
		
		new ForceSpawnCommand();
		
		//Help command
		new MBCommand();
		//History command
		new HistoryCommand();
		
		//Admin commands
		new MBImportCommand();
		new MBExportCommand();
		new MBDebug();
		new ReloadCommand();
		
		//MBWhitelist
		new WhitelistCommand();
		
		//Rangeban
		new RangeBanCommand();
		new TempRangeBanCommand();
		new UnbanRangeCommand();
    }
    
    public void startMetrics(){
        try{
        	if(metrics != null) return; //Don't start two metrics. 
        	
        	metrics = new Metrics(this);
        	if(metrics.start() == false) return; //Metrics is opt-out.
        	
        	Graph bans = metrics.createGraph("Bans");
        	Graph ipbans = metrics.createGraph("IP Bans");
        	Graph mutes = metrics.createGraph("Mutes");
        	
        	bans.addPlotter(new Metrics.Plotter() {
				@Override
				public int getValue() {
					return getBanManager().getBans().size();
				}
			});
        	
        	ipbans.addPlotter(new Metrics.Plotter() {
				@Override
				public int getValue() {
					return getBanManager().getIPBans().size();
				}
			});
        	
        	mutes.addPlotter(new Metrics.Plotter() {
				@Override
				public int getValue() {
					return getBanManager().getMutes().size();
				}
			});
        	
        }
        catch(IOException e){
        	e.printStackTrace();
        	System.out.println("Metrics start failed");
        }
    }
    /** Returns the metrics object for MaxBans */
    public Metrics getMetrics(){ return metrics; }
    /** The syncer for sending messages to other SyncServers */
    public Syncer getSyncer(){ return this.syncer; }
}
