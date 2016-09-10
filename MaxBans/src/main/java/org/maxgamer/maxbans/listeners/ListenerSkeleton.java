package org.maxgamer.maxbans.listeners;

import org.maxgamer.maxbans.MaxBans;
import org.bukkit.event.Listener;

public class ListenerSkeleton implements Listener
{
    protected MaxBans getPlugin() {
        return MaxBans.instance;
    }
}
