package org.maxgamer.maxbans;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.commands.*;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.listeners.*;
import org.maxgamer.maxbans.util.Formatter;
import org.maxgamer.maxbans.util.Metrics;
import org.maxgamer.maxbans.util.Metrics.Graph;

public class MaxBans extends JavaPlugin{
    private BanManager banManager;
    
    private BanCommand banCommand;
    private IPBanCommand ipBanCommand;
    private MuteCommand muteCommand;
    
    private TempBanCommand tempBanCommand;
    private TempIPBanCommand tempIPBanCommand;
    private TempMuteCommand tempMuteCommand;
    
    private UnbanCommand unbanCommand;
    private UnMuteCommand unMuteCommand;
    
    private CheckIPCommand checkIPCommand;
    private CheckBanCommand checkBanCommand;
    private DupeIPCommand dupeIPCommand;
    
    private WarnCommand warnCommand;
    private ClearWarningsCommand clearWarningsCommand;
    
    private LockdownCommand lockdownCommand;
    private KickCommand kickCommand;
    private HistoryCommand historyCommand;
    private ForceSpawnCommand forceSpawnCommand;
    
    private ReloadCommand reloadCommand;
    private MBCommand mbCommand;
    
    private MBImportCommand importCommand;
    private MBExportCommand exportCommand;
    private MBDebug mbDebugCommand;
            
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
		this.getConfig().options().copyDefaults();
		
		this.filter_names = getConfig().getBoolean("filter-names");
		
		Formatter.load(this);
		
		if(getConfig().getBoolean("database.mysql", false)){
			getLogger().info("Using MySQL");
			String user = getConfig().getString("database.user");
			String pass = getConfig().getString("database.pass");
			String host = getConfig().getString("database.host");
			String name = getConfig().getString("database.name");
			String port = getConfig().getString("database.port");
			db = new Database(this, host, name, user, pass, port);
		}
		else{
			getLogger().info("Using SQLite");
			//The database for bans
			db = new Database(this, new File(this.getDataFolder(), "bans.db"));
		}
		//Creates the tables if they don't exist
		try{
			db.createTables();
		}
		catch(SQLException e){
			e.printStackTrace();
			getLogger().severe("Could not create/check tables! Startup failed.");
			return;
		}
		
		//BanManager
		banManager = new BanManager(this);
		
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
		this.joinListener = new JoinListener(this);
		this.chatCommandListener = new ChatCommandListener(this);
        
        Bukkit.getServer().getPluginManager().registerEvents(this.joinListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.chatCommandListener, this);
        
        startMetrics();
    }
	
	public void onDisable(){
		this.getLogger().info("Disabling Maxbans...");
		this.db.getDatabaseWatcher().run(); //Empties buffer
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
    	this.banCommand = new BanCommand(this);
		this.ipBanCommand = new IPBanCommand(this);
		this.muteCommand = new MuteCommand(this);
		
		this.tempBanCommand = new TempBanCommand(this);
		this.tempIPBanCommand = new TempIPBanCommand(this);
		this.tempMuteCommand = new TempMuteCommand(this);
		
		this.unbanCommand = new UnbanCommand(this);
		this.unMuteCommand = new UnMuteCommand(this);
		
		this.checkIPCommand = new CheckIPCommand(this);
		this.checkBanCommand = new CheckBanCommand(this);
		this.dupeIPCommand = new DupeIPCommand(this);
		
		this.warnCommand = new WarnCommand(this);
		this.clearWarningsCommand = new ClearWarningsCommand(this);
		
		this.lockdownCommand = new LockdownCommand(this);
		this.kickCommand = new KickCommand(this);
		
		this.forceSpawnCommand = new ForceSpawnCommand();
		this.historyCommand = new HistoryCommand(this);
		
		this.reloadCommand = new ReloadCommand(this);
		this.mbCommand = new MBCommand();
		
		this.importCommand = new MBImportCommand(this);
		this.exportCommand = new MBExportCommand(this);
		this.mbDebugCommand = new MBDebug();
		
		//Register commands
		this.getCommand("ban").setExecutor(banCommand);
		this.getCommand("ipban").setExecutor(ipBanCommand);
		this.getCommand("mute").setExecutor(muteCommand);
		
		this.getCommand("tempban").setExecutor(tempBanCommand);
		this.getCommand("tempipban").setExecutor(tempIPBanCommand);
		this.getCommand("tempmute").setExecutor(tempMuteCommand);
		
		this.getCommand("unban").setExecutor(unbanCommand);
		this.getCommand("unmute").setExecutor(unMuteCommand);
		
		this.getCommand("checkip").setExecutor(checkIPCommand);
		this.getCommand("checkban").setExecutor(checkBanCommand);
		this.getCommand("dupeip").setExecutor(dupeIPCommand);
		
		this.getCommand("warn").setExecutor(warnCommand);
		this.getCommand("clearwarnings").setExecutor(clearWarningsCommand);
		
		this.getCommand("lockdown").setExecutor(lockdownCommand);
		this.getCommand("kick").setExecutor(kickCommand);
		this.getCommand("forcespawn").setExecutor(forceSpawnCommand);
		this.getCommand("history").setExecutor(historyCommand);
		
		this.getCommand("mbreload").setExecutor(reloadCommand);
		this.getCommand("mb").setExecutor(mbCommand);
		
		this.getCommand("mbimport").setExecutor(importCommand);
		this.getCommand("mbexport").setExecutor(exportCommand);
		this.getCommand("mbdebug").setExecutor(mbDebugCommand);
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
}
