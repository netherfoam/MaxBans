package org.maxgamer.maxbans.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.maxgamer.maxbans.MaxBans;

public class PluginListener implements Listener{
	private MaxBans plugin;
	public PluginListener(MaxBans plugin){
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPluginLoad(PluginEnableEvent e){
		if(e.getPlugin().getName().equalsIgnoreCase("UltraBan")){
			plugin.getLogger().info("Detected UB load...");
			plugin.getBanManager().reload();
		}
	}
}