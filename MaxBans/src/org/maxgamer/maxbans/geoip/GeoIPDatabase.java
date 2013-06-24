package org.maxgamer.maxbans.geoip;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.maxgamer.maxbans.util.IPAddress;

public class GeoIPDatabase{
	public static final byte IP_VALUE_POS = 0;
	public static final byte IP_COUNTRY_CODE_POS = 1;
	public static final byte IP_COUNTRY_POS = 1;
	
	private TreeSet<GeoIP> info;
	private File file;
	
	/**
	 * Reloads this GeoIPDatabase from file.
	 */
	public void reload(){
		this.info = new TreeSet<GeoIP>();
		
		if(file == null || !file.exists()){
			System.out.println("WARNING: Could not load GeoIPDatabase. The file does not exist.");
			return;
		}
		
		Scanner sc;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		Pattern pattern = Pattern.compile("(\"[^\"]*\")|([^\\,]+)"); //Works with both CSV formats which use "value","value" and ones that use value,value
		int line = 0;
		while(sc.hasNextLine()){
			line++;
			try{
				String s = sc.nextLine();
				Matcher matcher = pattern.matcher(s);
				
				LinkedList<String> strs = new LinkedList<String>();
				while(matcher.find()){
					strs.add(matcher.group());
				}
				String[] p = strs.toArray(new String[strs.size()]);
				
				//Remove the "" around everything if necessary.
				for(int i = 0; i < p.length; i++){
					int start = 0;
					int end = p[i].length();
					if(p[i].endsWith("\"")){
						end -= 1;
					}
					if(p[i].startsWith("\"")){
						start += 1;
					}
					p[i] = p[i].substring(start, end);
				}
				GeoIP ip = new GeoIP(Long.parseLong(p[IP_VALUE_POS]), p[IP_COUNTRY_POS]);
				info.add(ip);
			}
			catch(Exception e){
				System.out.println("Error on line #" + line + " of GeoIPDatabase.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Constructs and loads a new GeoIPDatabase from the given CSV file.
	 * @param csv The CSV file.
	 */
	public GeoIPDatabase(File csv){
		this.file = csv;
		reload();
	}
	
	/**
	 * Returns the string that the given IP address is from.
	 * This will throw NumberFormatException if the IP address can't be
	 * parsed. 
	 * @param ip The IP address to look up.
	 * @return The country, or "NULL" if the country is unknown.
	 */
	public String getCountry(String ip){
		IPAddress data = new IPAddress(ip);
		long value = getCode(data);
		
		GeoIP dummy = new GeoIP(value, "dummy");
		GeoIP result = info.floor(dummy);
		if(result == null) return "NULL";
		else return result.getCountry();
	}
	
	/**
	 * Converts the given IPAddress into a long value.  Value is taken using the following pseudo code:<br/>
	 <br/>
	 	address = '174.36.207.186'<br/>
		( o1, o2, o3, o4 ) = address.split('.')<br/>
		<br/>
		integer_ip =   ( 16777216 * o1 )<br/>
		             + (    65536 * o2 )<br/>
		             + (      256 * o3 )<br/>
		             +              o4<br/>
		             
	 * @param data The IP Address
	 * @return The long representation.
	 */
	private long getCode(IPAddress data){
		int[] bytes = data.getBytes();
		long value = 0;
		value += 16777216L * bytes[0];
		value += 65536L * bytes[1];
		value += 256L * bytes[2];
		value += bytes[3];
		return value;
	}
}