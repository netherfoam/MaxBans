//Push test by Darek
package org.maxgamer.maxbans;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.maxgamer.maxbans.banmanager.BanManager;
import org.maxgamer.maxbans.listeners.*;

public class MaxBans extends JavaPlugin{
        private BanManager banManager;
        private ChatListener chatListener;
        private CommandListener commandListener;
        private JoinListener joinListener; 
	public void onEnable(){
            
            Bukkit.getServer().getPluginManager().registerEvents(this.chatListener, this);
            Bukkit.getServer().getPluginManager().registerEvents(this.commandListener, this);
            Bukkit.getServer().getPluginManager().registerEvents(this.joinListener, this);
            banManager = new BanManager(this);
        }
	public void onDisable(){
		
	}
        public BanManager getBanManager() {
            return banManager;
        }
        
}
