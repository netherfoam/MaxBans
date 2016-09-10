package org.maxgamer.maxbans.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.ChatColor;

public class Formatter
{
    public static ChatColor primary;
    public static ChatColor secondary;
    public static ChatColor regular;
    public static ChatColor banner;
    public static ChatColor reason;
    public static ChatColor time;
    
    public static void load(final Plugin plugin) {
        Formatter.primary = getColor(plugin.getConfig().getString("color.primary"));
        Formatter.secondary = getColor(plugin.getConfig().getString("color.secondary"));
        final ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("kick-colors");
        Formatter.regular = getColor(cfg.getString("regular"));
        Formatter.banner = getColor(cfg.getString("banner"));
        Formatter.reason = getColor(cfg.getString("reason"));
        Formatter.time = getColor(cfg.getString("time"));
    }
    
    public static ChatColor getColor(final String s) {
        ChatColor col = ChatColor.getByChar(s);
        if (col != null) {
            return col;
        }
        col = ChatColor.valueOf(s.toUpperCase());
        if (col != null) {
            return col;
        }
        return ChatColor.WHITE;
    }
}
