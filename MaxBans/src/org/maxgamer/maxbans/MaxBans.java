package org.maxgamer.maxbans;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.banmanager.SyncBanManager;
import org.maxgamer.maxbans.bungee.BungeeListener;
import org.maxgamer.maxbans.commands.BanCommand;
import org.maxgamer.maxbans.commands.CheckBanCommand;
import org.maxgamer.maxbans.commands.CheckIPCommand;
import org.maxgamer.maxbans.commands.ClearWarningsCommand;
import org.maxgamer.maxbans.commands.DupeIPCommand;
import org.maxgamer.maxbans.commands.ForceSpawnCommand;
import org.maxgamer.maxbans.commands.HistoryCommand;
import org.maxgamer.maxbans.commands.IPBanCommand;
import org.maxgamer.maxbans.commands.ImmuneCommand;
import org.maxgamer.maxbans.commands.KickCommand;
import org.maxgamer.maxbans.commands.LockdownCommand;
import org.maxgamer.maxbans.commands.MBCommand;
import org.maxgamer.maxbans.commands.MBDebugCommand;
import org.maxgamer.maxbans.commands.MBExportCommand;
import org.maxgamer.maxbans.commands.MBImportCommand;
import org.maxgamer.maxbans.commands.MuteCommand;
import org.maxgamer.maxbans.commands.RangeBanCommand;
import org.maxgamer.maxbans.commands.ReloadCommand;
import org.maxgamer.maxbans.commands.TempBanCommand;
import org.maxgamer.maxbans.commands.TempIPBanCommand;
import org.maxgamer.maxbans.commands.TempMuteCommand;
import org.maxgamer.maxbans.commands.TempRangeBanCommand;
import org.maxgamer.maxbans.commands.UnMuteCommand;
import org.maxgamer.maxbans.commands.UnWarnCommand;
import org.maxgamer.maxbans.commands.UnbanCommand;
import org.maxgamer.maxbans.commands.UnbanRangeCommand;
import org.maxgamer.maxbans.commands.WarnCommand;
import org.maxgamer.maxbans.commands.WhitelistCommand;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.database.Database.ConnectionException;
import org.maxgamer.maxbans.database.DatabaseCore;
import org.maxgamer.maxbans.database.MySQLCore;
import org.maxgamer.maxbans.database.SQLiteCore;
import org.maxgamer.maxbans.geoip.GeoIPDatabase;
import org.maxgamer.maxbans.listeners.ChatCommandListener;
import org.maxgamer.maxbans.listeners.ChatListener;
import org.maxgamer.maxbans.listeners.HeroChatListener;
import org.maxgamer.maxbans.listeners.JoinListener;
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
	public static final String BUNGEE_CHANNEL = "BungeeCord";
	
    private BanManager banManager;
    private Syncer syncer;
    private SyncServer syncServer;
    private GeoIPDatabase geoIPDB;
            
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
    
    public GeoIPDatabase getGeoDB(){
    	return geoIPDB;
    }
    
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
		Msg.reload();
		
		this.getConfig().options().copyDefaults();
		
		final File geoCSV = new File(this.getDataFolder(), "geoip.csv");
		if(!geoCSV.exists()){
			Runnable download = new Runnable(){
				@Override
				public void run(){
					String url = "http://maxgamer.org/plugins/maxbans/geoip.csv";
					
					getLogger().info("Downloading geoIPDatabase...");
					try{
						FileOutputStream out = new FileOutputStream(geoCSV);
						BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
					    byte data[] = new byte[1024];
					    int count;
					    while((count = in.read(data,0,1024)) != -1){
					    	out.write(data, 0, count);
						}
					    getLogger().info("Download complete.");
					    out.close();
					    in.close();
					    geoIPDB = new GeoIPDatabase(geoCSV);
					}
					catch(Exception e){
						e.printStackTrace();
						System.out.println("Failed to download MaxBans GeoIPDatabase");
					}
				}
			};
			Bukkit.getScheduler().runTaskAsynchronously(this, download);
		}
		else{
			this.geoIPDB = new GeoIPDatabase(geoCSV);
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
		
		
		ConfigurationSection syncConfig = this.getConfig().getConfigurationSection("sync");
		if(syncConfig.getBoolean("use", false)){
			getLogger().info("Using Sync.");
			final String host = syncConfig.getString("host");
			final int port = syncConfig.getInt("port");
			final String pass = syncConfig.getString("pass");
			
			if(syncConfig.getBoolean("server", false)){
				try{
					this.syncServer = new SyncServer(port, pass);
					this.syncServer.start();
				}
				catch(IOException e){
					e.printStackTrace();
					getLogger().info("Could not start sync server!");
				}
			}
			
			syncer = new Syncer(host, port, pass);
			syncer.start();
			//Special sync ban manager
			banManager = new SyncBanManager(this);
		}
		else{
			//BanManager
			banManager = new BanManager(this);
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
        
        if(this.isBungee()){
        	//Incoming (Results for IPs)
        	Bukkit.getMessenger().registerIncomingPluginChannel(this, MaxBans.BUNGEE_CHANNEL, new BungeeListener());
        	//Outgoing (Requests for IPs)
        	Bukkit.getMessenger().registerOutgoingPluginChannel(this, MaxBans.BUNGEE_CHANNEL);
        }
    }
	
	public boolean isBungee(){
		return MaxBans.instance.getConfig().getBoolean("bungee");
	}
	
	public void onDisable(){
		this.getLogger().info("Disabling Maxbans...");
		
		if(syncer != null){
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
		new UnWarnCommand();
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
		new MBDebugCommand();
		new ReloadCommand();
		
		//MBWhitelist
		new WhitelistCommand();
		new ImmuneCommand();
		
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
