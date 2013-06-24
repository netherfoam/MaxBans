package org.maxgamer.maxbans.geoip;

public class GeoIP implements Comparable<GeoIP>{
	private long value;
	private String country;
	
	public GeoIP(long value, String country){
		this.value = value;
		this.country = country;
	}
	
	public String getCountry(){
		return country;
	}
	
	@Override
	public int compareTo(GeoIP o) {
		if(o.value > value){
			return -1;
		}
		else if(o.value < value){
			return 1;
		}
		return 0;
	}
	public long getValue(){
		return value;
	}
}