package org.maxgamer.maxbans.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

public class Formatter{
	/** The primary color for messages - Default is white */
	public static ChatColor primary;
	/** The secondary color for messages - Default is light green */
	public static ChatColor secondary;
	/** The regular text color. E.g. "Reason: ", "Time Remaining: ", etc.*/
	public static ChatColor regular;
	/** The color for the banner, e.g. Banned by "netherfoam".*/
	public static ChatColor banner;
	/** The reason for the ban - E.g. Banned for "Misconduct" */
	public static ChatColor reason;
	/** The time remaining - E.g. "4 hours 6 minutes" remaining */
	public static ChatColor time;
	
	public static void load(Plugin plugin){
		primary = getColor(plugin.getConfig().getString("color.primary"));
		secondary = getColor(plugin.getConfig().getString("color.secondary"));
		
		ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("kick-colors");
		regular = getColor(cfg.getString("regular"));
		banner = getColor(cfg.getString("banner"));
		reason = getColor(cfg.getString("reason"));
		time = getColor(cfg.getString("time"));
	}
	
	public static ChatColor getColor(String s){
		ChatColor col = ChatColor.getByChar(s);
		if(col != null) return col;
		col = ChatColor.valueOf(s.toUpperCase());
		if(col != null) return col;
		return ChatColor.WHITE; //Didn't give us a proper color.
	}
}