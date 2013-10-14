package org.maxgamer.maxbans;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public class Msg{
	private static YamlConfiguration cfg;
	
	public static void reload(){
		File f = new File(MaxBans.instance.getDataFolder(), "messages.yml");
		cfg = new YamlConfiguration();
		try{
			YamlConfiguration defaults = new YamlConfiguration();
			InputStream in = MaxBans.instance.getResource("messages.yml");
			defaults.load(in);
			in.close();
			
			if(f.exists()){
				cfg.load(f); //If the existing message file exists, load it as well.
			}
			else{
				//Save the file to disk if the messages.yml file doesn't exist yet.
				FileOutputStream out = new FileOutputStream(f);
				in = MaxBans.instance.getResource("messages.yml");
				byte[] buffer = new byte[1024];
				int len = in.read(buffer);
				while (len != -1) {
				    out.write(buffer, 0, len);
				    len = in.read(buffer);
				}
				in.close();
				out.close();
			}
			cfg.setDefaults(defaults);
		} catch(FileNotFoundException e){
			e.printStackTrace();
		} catch(InvalidConfigurationException e){
			e.printStackTrace();
			System.out.println("Invalid messages.yml config. Using defaults.");
			try {
				cfg.load(MaxBans.instance.getResource("messages.yml"));
			} catch (Exception ex){
				ex.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String loc, String[] keys, String[] values){
		String msg = cfg.getString(loc);
		
		if(msg == null || msg.isEmpty()){
			return "Unknown message in config: " + loc;
		}
		
		if(keys != null && values != null){
			if(keys.length != values.length){ //Dirty, but we don't want to break a ban or something.
				try{
					throw new IllegalArgumentException("Invalid message request. keys.length should equal values.length!");
				}
				catch(IllegalArgumentException e){
					e.printStackTrace();
				}
			}
			
			for(int i = 0; i < keys.length; i++){
				msg = msg.replace("{"+keys[i]+"}", values[i]); //I could do case insensitive, but nty.
			}
		}
		
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	public static String get(String loc){
		return get(loc, (String[])null, (String[])null);
	}
	public static String get(String loc, String key, String value){
		return get(loc, new String[]{key}, new String[]{value});
	}
	
}