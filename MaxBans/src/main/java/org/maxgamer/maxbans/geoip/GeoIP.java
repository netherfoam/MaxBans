package org.maxgamer.maxbans.geoip;

public class GeoIP implements Comparable<GeoIP>
{
    private long value;
    private String country;
    
    public GeoIP(final long value, final String country) {
        super();
        this.value = value;
        this.country = country;
    }
    
    public String getCountry() {
        return this.country;
    }
    
    public int compareTo(final GeoIP o) {
        if (o.value > this.value) {
            return -1;
        }
        if (o.value < this.value) {
            return 1;
        }
        return 0;
    }
    
    public long getValue() {
        return this.value;
    }
}
