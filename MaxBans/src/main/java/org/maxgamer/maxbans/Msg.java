package org.maxgamer.maxbans;

import org.bukkit.ChatColor;

import java.io.InputStream;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

import java.io.FileNotFoundException;

import org.bukkit.configuration.Configuration;

import java.io.FileOutputStream;
import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

public class Msg
{
    private static YamlConfiguration cfg;
    
    @SuppressWarnings("deprecation")
	public static void reload() {
        final File f = new File(MaxBans.instance.getDataFolder(), "messages.yml");
        Msg.cfg = new YamlConfiguration();
        try {
            final YamlConfiguration defaults = new YamlConfiguration();
            InputStream in = MaxBans.instance.getResource("messages.yml");
            defaults.load(in);
            in.close();
            if (f.exists()) {
                Msg.cfg.load(f);
            }
            else {
                final FileOutputStream out = new FileOutputStream(f);
                in = MaxBans.instance.getResource("messages.yml");
                final byte[] buffer = new byte[1024];
                for (int len = in.read(buffer); len != -1; len = in.read(buffer)) {
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            }
            Msg.cfg.setDefaults((Configuration)defaults);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (InvalidConfigurationException e2) {
            e2.printStackTrace();
            System.out.println("Invalid messages.yml config. Using defaults.");
            try {
                Msg.cfg.load(MaxBans.instance.getResource("messages.yml"));
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        catch (IOException e3) {
            e3.printStackTrace();
        }
    }
    
    public static String get(final String loc, final String[] keys, final String[] values) {
        String msg = Msg.cfg.getString(loc);
        if (msg == null || msg.isEmpty()) {
            return "Unknown message in config: " + loc;
        }
        if (keys != null && values != null) {
            if (keys.length != values.length) {
                try {
                    throw new IllegalArgumentException("Invalid message request. keys.length should equal values.length!");
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < keys.length; ++i) {
                msg = msg.replace("{" + keys[i] + "}", values[i]);
            }
        }
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
    
    public static String get(final String loc) {
        return get(loc, null, (String[])null);
    }
    
    public static String get(final String loc, final String key, final String value) {
        return get(loc, new String[] { key }, new String[] { value });
    }
}
