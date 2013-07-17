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
			int result = compareVersion(sc.nextLine(), MaxBans.instance.getDescription().getVersion());
			
			if(result > 0){
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
	
	/**
	 * Compares two string versions
	 * @param v1 The first version string eg 5.9
	 * @param v2 The second version string eg 5.15
	 * @return 0 if they're the same version (or one is invalid), 1 if v1 is newer, -1 if v2 is newer.
	 */
	public static int compareVersion(String v1, String v2){
		v1 = v1.replaceAll("[^0-9\\.]", "");
		v2 = v2.replaceAll("[^0-9\\.]", "");
		
		String[] v1s = v1.split("\\.");
		String[] v2s = v2.split("\\.");
		
		int result = 0;
		
		int pos = 0;
		while(result == 0){
			if(pos >= v1s.length && pos >= v2s.length){
				break; //Out of things to look at
			}
			else if(pos >= v1s.length){
				//i1 is exhausted
				result = -1;
			}
			else if(pos >= v2s.length){
				//i2 is exhausted
				result = 1;
			}
			else{
				int i1;
				int i2;
				
				i1 = Integer.parseInt(v1s[pos]);
				i2 = Integer.parseInt(v2s[pos]);
				
				if(i1 > i2) result = 1;
				else if(i2 > i1) result = -1;
			}
			pos++;
		}
		return result;
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