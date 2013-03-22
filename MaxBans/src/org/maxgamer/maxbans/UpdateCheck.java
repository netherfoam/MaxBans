package org.maxgamer.maxbans;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class UpdateCheck{
	private static final String address = "http://maxgamer.org/plugins/maxbans/updateCheck.php";
	
	public static boolean hasUpdate(){
		String s = getURLContents();
		Scanner sc = new Scanner(s);
		
		try{
			int version = getCurrentVersion();
			int latest = parse(sc.nextLine());
			if(latest > version){
				return true;
			}
		}
		catch(NoSuchElementException e){
			//Fail silently.
			//MaxBans.instance.getLogger().info("Update server sent malformed data.");
			//e.printStackTrace();
		}
		return false;
	}
	public static int getCurrentVersion(){
		if(MaxBans.instance == null){
			return Integer.MAX_VALUE;
		}
		return parse(MaxBans.instance.getDescription().getVersion()); //Deletes anything that's not 0-9.
	}
	private static int parse(String version){
		return Integer.parseInt(version.replaceAll("[^0-9]", "")); //Deletes anything that's not 0-9.
	}
	
	public static String getURLContents(){
		URL url;
		try {
			url = new URL(address); //Throws malformed URL
			
			InputStream is = url.openStream();
			int ptr = 0;
			StringBuffer buffer = new StringBuffer();
			while ((ptr = is.read()) != -1) {
			    buffer.append((char)ptr);
			}
			
			return buffer.toString();
		} 
		catch (MalformedURLException e) {
			//Fail silently
			//MaxBans.instance.getLogger().info("Could not contact update server.");
			//e.printStackTrace();
		}
		catch(IOException e){
			//Fail silently.
			//MaxBans.instance.getLogger().info("Could not contact update server.");
			//e.printStackTrace();
		}
		return "";
	}
}