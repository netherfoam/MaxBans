package org.maxgamer.maxbans.commands;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MBDebugCommand extends CmdSkeleton{
	public MBDebugCommand(){
		super("mbdebug", "maxbans.debug");
		namePos = -1;
		minArgs = 1;
	}
	public boolean run(final CommandSender sender, Command command, String label, String[] args) {
		Printable out;
		if(args[0].equalsIgnoreCase("file")){
			File file = new File(plugin.getDataFolder(), "debug.txt");
			try {
				file.createNewFile();
				final PrintStream ps = new PrintStream(file);
				
				out = new Printable(){
					public void print(String s){
						ps.println(s);
					}
				};
				
			} catch (IOException e) {
				sender.sendMessage("Failed to create debug file");
				e.printStackTrace();
				return true;
			}
		}
		else if(args[0].equalsIgnoreCase("console")){
			out = new Printable(){
				CommandSender sender = Bukkit.getConsoleSender();
				public void print(String s){
					sender.sendMessage(s);
				}
			};
		}
		else if(args[0].equalsIgnoreCase("chat")){
			out = new Printable(){
				public void print(String s){
					sender.sendMessage(s);
				}
			};
		}
		else{
			sender.sendMessage("No such output method... " + args[0]);
			sender.sendMessage("/mbdebug file - Outputs debug info to Server\\plugins\\MaxBans\\debug.txt");
			sender.sendMessage("/mbdebug chat - Outputs debug info to chat");
			sender.sendMessage("/mbdebug console - Outputs debug info to Console");
			return true;
		}
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println();
		out.print("MaxBans Data Dump - " + dateFormat.format(date));
		out.print("Bukkit: " + Bukkit.getServer().getBukkitVersion());
		out.print("Plugin: " + plugin.toString());
		out.print("IP: " + Bukkit.getIp() + ":" + Bukkit.getPort());
		out.print("Server: " + Bukkit.getServerName());
		out.print("Online-mode: " + Bukkit.getServer().getOnlineMode());
		out.print("Server String: " + Bukkit.getServer().toString());
		out.print("Package: " + Bukkit.getServer().getClass().getCanonicalName());
		
		out.print("=== Config File ===");
		//We want to make sure the password isn't put in debug info..
		String pass = plugin.getConfig().getString("database.pass"); //Store it
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < pass.length(); i++) sb.append('*'); //Build a string of equal length with *'s instead
		plugin.getConfig().set("database.pass", sb.toString()); //Put the replacement ***'s in
		
		String syncpass = plugin.getConfig().getString("sync.pass");
		sb = new StringBuilder();
		for(int i = 0; i < syncpass.length(); i++) sb.append('*'); //Build a string of equal length with *'s instead
		plugin.getConfig().set("sync.pass", sb.toString());
		
		//Now we can save the config to the file
		out.print(plugin.getConfig().saveToString()); //Output the config with the censored passwords
		plugin.getConfig().set("database.pass", pass); //Put the ***'s back in the config.
		plugin.getConfig().set("sync.pass", pass); //And the sync pass
		
		out.print("=== Vanilla Bans (MaxBans does not handle these) ===");
		out.print(Bukkit.getBannedPlayers());
		
		out.print("=== Online Players ===");
		out.print(Bukkit.getOnlinePlayers());
		out.print("=== Plugins ===");
		out.print(Bukkit.getPluginManager().getPlugins());
		out.print("=== Whitelisted Players ===");
		out.print(Bukkit.getWhitelistedPlayers());
		out.print("=== Immune Players ===");
		out.print(plugin.getBanManager().getImmunities());
		out.print("=== Worlds ===");
		out.print(Bukkit.getWorlds());
		
		
		out.print("=== Bans ===");
		out.print(plugin.getBanManager().getBans());
		out.print("=== IP Bans ===");
		out.print(plugin.getBanManager().getIPBans());
		out.print("=== Mutes ===");
		out.print(plugin.getBanManager().getMutes());
		
		out.print("=== Temp Bans ===");
		out.print(plugin.getBanManager().getTempBans());
		out.print("=== Temp IP Bans ===");
		out.print(plugin.getBanManager().getTempIPBans());
		out.print("=== Temp Mutes ===");
		out.print(plugin.getBanManager().getTempMutes());
		
		out.print("=== IP Recordings ===");
		out.print(plugin.getBanManager().getIPHistory());
		out.print("=== Player Names ===");
		out.print(plugin.getBanManager().getPlayers());
		
		out.print("=== RangeBans ===");
		out.print(plugin.getBanManager().getRangeBans());
		
		out.print("=== IP Whitelist ===");
		out.print(plugin.getBanManager().getWhitelist());
		
		out.print("=== History (Top=Recent) ===");
		out.print(plugin.getBanManager().getHistory());
		
		if(plugin.getBanManager().getDNSBL() != null){
			out.print("=== DNSBL Records ===");
			out.print(plugin.getBanManager().getDNSBL().getHistory());
			out.print("=== DNSBL Servers ===");
			out.print(plugin.getBanManager().getDNSBL().getServers());
		}
		
		return true;
	}
	
	public abstract class Printable{
		public abstract void print(String s);
		public void print(Map<?, ?> map){
			print("Map: " + map.getClass().getCanonicalName() + ", Size: " + map.size());
			for(Entry<?, ?> entry : map.entrySet()){
				String s = String.format("%-16s | %.1000s", String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
				print(s);
			}
		}
		public void print(Collection<?> list){
			print("Collection: " + list.getClass().getCanonicalName() + ", Size: " + list.size());
			for(Object value : list){
				print(String.valueOf(value));
			}
		}
		public <T> void print(T[] array){
			print("List: " + array.getClass().getCanonicalName() + ", Length: " + array.length);
			for(T t : array){
				print(String.valueOf(t));
			}
		}
	}
}