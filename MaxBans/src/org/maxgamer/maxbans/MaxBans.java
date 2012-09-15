//Push test by Darek
package org.maxgamer.maxbans;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.database.Database;
import org.maxgamer.maxbans.listeners.*;

public class MaxBans extends JavaPlugin{
        private BanManager banManager;
        private ChatListener chatListener;
        private CommandListener commandListener;
        private JoinListener joinListener; 
        private Database db;
        
	public void onEnable(){
		if(!this.getDataFolder().exists()){
			this.getDataFolder().mkdir();
		}
		File configFile = new File(this.getDataFolder(), "config.yml");
		
		if(!configFile.exists()){
			this.saveResource("config.yml", false);
		}
		//Is this necessary?
		this.reloadConfig();
		
		db = new Database(this, new File(this.getDataFolder(), "bans.db"));
		
		banManager = new BanManager(this);
		
	
        Bukkit.getServer().getPluginManager().registerEvents(this.chatListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.commandListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(this.joinListener, this);
        
    }
	public void onDisable(){
		
	}
    public BanManager getBanManager() {
        return banManager;
    }
        
    public Database getDB(){
    	return db;
    }
}
