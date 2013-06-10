package org.maxgamer.maxbans.listeners;

import org.bukkit.event.Listener;
import org.maxgamer.maxbans.MaxBans;

public class ListenerSkeleton implements Listener{
	//protected MaxBans plugin = MaxBans.instance;
	protected MaxBans getPlugin(){
		return MaxBans.instance;
	}
}