//Push test by Darek
package org.maxgamer.maxbans;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.commands.*;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.listeners.*;

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
        private ForceSpawnCommand forceSpawnCommand;
        
        private ReloadCommand reloadCommand;
                
        private JoinListener joinListener;
        private HeroChatListener herochatListener; 
        private ChatListener chatListener;
        //private PluginListener pluginListener;
        
        private Database db;
        
        public ChatColor color_primary;
        public ChatColor color_secondary;
        
	public void onEnable(){
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
		
		//this.color_primary = ChatColor.GREEN;
		//this.color_secondary = ChatColor.WHITE;
		
		this.color_primary = ChatColor.getByChar(getConfig().getString("color.primary"));
		this.color_secondary = ChatColor.getByChar(getConfig().getString("color.secondary"));
		
		//The database for bans
		db = new Database(this, new File(this.getDataFolder(), "bans.db"));
		//Creates the tables if they don't exist
		db.createTables();
		
		//BanManager
		banManager = new BanManager(this);
		
		//Commands
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
		
		Location spawn = new Location(Bukkit.getWorld(getConfig().getString("spawn.world")), getConfig().getDouble("spawn.x"), getConfig().getDouble("spawn.y"), getConfig().getDouble("spawn.z"));
		this.forceSpawnCommand = new ForceSpawnCommand(this, spawn);
		
		this.reloadCommand = new ReloadCommand(this);
		
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
		
		this.getCommand("mbreload").setExecutor(reloadCommand);
		
		//Listeners for chat (mute) and join (Ban)
		if(Bukkit.getPluginManager().getPlugin("HeroChat") != null){
			this.herochatListener = new HeroChatListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(this.herochatListener, this);
		}
		else{
			this.chatListener = new ChatListener(this);
			Bukkit.getServer().getPluginManager().registerEvents(this.chatListener, this);
		}
		this.joinListener = new JoinListener(this);
        
        Bukkit.getServer().getPluginManager().registerEvents(this.joinListener, this);
    }
	public void onDisable(){
		this.getLogger().info("Disabling Maxbans...");
		this.db.getDatabaseWatcher().stop(); //Pauses
		this.db.getDatabaseWatcher().run(); //Empties buffer
		this.getLogger().info("Cleared buffer...");
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
}
