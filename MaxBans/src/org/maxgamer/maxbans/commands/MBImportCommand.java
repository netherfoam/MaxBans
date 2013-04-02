package org.maxgamer.maxbans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.maxgamer.maxbans.commands.bridge.DynamicBanBridge;
import org.maxgamer.maxbans.commands.bridge.VanillaBridge;
import org.maxgamer.maxbans.util.Formatter;


public class MBImportCommand extends CmdSkeleton{
    public MBImportCommand(){
        super("mbimport", "maxbans.import");
        namePos = -1;
    }
    
	public boolean run(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 0){
			sender.sendMessage(Formatter.primary + "MaxBans Importer:");
			sender.sendMessage(Formatter.secondary + "/mbimport vanilla " + Formatter.primary + " - Imports vanilla bans.");
			sender.sendMessage(Formatter.secondary + "/mbimport dynamicban " + Formatter.primary + " - Imports dynamicBan bans.");
		}
		else{
			if(args[0].equalsIgnoreCase("vanilla")){
				VanillaBridge bridge = new VanillaBridge();
				bridge.load();
				
				sender.sendMessage(Formatter.secondary + "Success.");
			}
			else if(args[0].equalsIgnoreCase("dynamicban")){
				sender.sendMessage(ChatColor.RED + "Importing bans. This may take a while!");
				try{
					DynamicBanBridge bridge = new DynamicBanBridge();
					bridge.load();
					
					sender.sendMessage(ChatColor.GREEN + "Import successful!");
				}
				catch(Exception e){
					sender.sendMessage(ChatColor.RED + "Error importing: " + e.getMessage());
				}
			}
			else{
				sender.sendMessage(Formatter.secondary + "Failed.  No known importer: " + args[0]);
			}
		}
		
		return true;
	}
}
