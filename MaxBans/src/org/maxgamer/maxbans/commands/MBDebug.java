package org.maxgamer.maxbans.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MBDebug extends CmdSkeleton{
	public MBDebug(){
		super("mbdebug", "maxbans.debug");
		namePos = -1;
	}
	public boolean run(CommandSender sender, Command command, String label, String[] args) {
		Map<String, Map<String, ?>> maps = new LinkedHashMap<String, Map<String, ?>>();
		maps.put("Bans", plugin.getBanManager().getBans());
		maps.put("IP Bans", plugin.getBanManager().getIPBans());
		maps.put("Mutes", plugin.getBanManager().getMutes());
		
		maps.put("Temp Bans", plugin.getBanManager().getTempBans());
		maps.put("Temp IP Bans", plugin.getBanManager().getTempIPBans());
		maps.put("Temp Mutes", plugin.getBanManager().getTempMutes());
		
		maps.put("IP History", plugin.getBanManager().getIPHistory());
		maps.put("Players", plugin.getBanManager().getPlayers());
		
		if(args.length <= 0){
			print(sender, maps);
		}
		else if(args[0].equalsIgnoreCase("chat")){
			print(sender, maps);
		}
		else if(args[0].equalsIgnoreCase("console")){
			print(Bukkit.getConsoleSender(), maps);
		}
		else if(args[0].equalsIgnoreCase("file")){
			File file = new File(plugin.getDataFolder(), "debug.txt");
			PrintStream ps;
			try {
				file.createNewFile();
				ps = new PrintStream(file);
			} catch (IOException e) {
				sender.sendMessage("Failed to create debug file");
				e.printStackTrace();
				return true;
			}
			
			for(Entry<String, Map<String, ?>> entry : maps.entrySet()){
				String type = entry.getKey();
				Map<String, ?> map = entry.getValue();
				ps.println("=== " + type + " ===");
				for(Entry<String, ?> mapEntry : map.entrySet()){
					ps.println(String.format("%-16s | %.100s", mapEntry.getKey(), mapEntry.getValue().toString()));
				}
			}
			sender.sendMessage("Created debug log: " + file.toString());
		}
		else{
			sender.sendMessage("Valid arguments/outputs: chat|console|file. Not " + args[0]);
		}
		return true;
	}
	
	private void print(CommandSender sender, Map<String, Map<String, ?>> maps){
		for(Entry<String, Map<String, ?>> entry : maps.entrySet()){
			String type = entry.getKey();
			Map<String, ?> map = entry.getValue();
			sender.sendMessage("=== " + type + " ===");
			for(Entry<String, ?> mapEntry : map.entrySet()){
				sender.sendMessage(String.format("%-16s | %.100s", mapEntry.getKey(), mapEntry.getValue().toString()));
			}
		}
	}
}