package org.maxgamer.maxbans.listeners;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.MaxBans;

public class CommandListener implements CommandExecutor{
        private MaxBans plugin;
        public CommandListener(MaxBans mb){
            plugin=mb;
        }
		public boolean onCommand(CommandSender sender, Command command, String arg2, String[] args) {
			// TODO Auto-generated method stub
			return false;
		}
            
}
