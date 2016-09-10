package org.maxgamer.maxbans.util;

import java.util.Arrays;

public class IPAddress implements Comparable<IPAddress>
{
    private int[] bytes;
    
    public IPAddress(final String s) {
        super();
        this.bytes = new int[4];
        final String[] t = s.split("\\.");
        if (t.length != 4) {
            throw new NumberFormatException("The given IP was invalid! (" + s + ") (Only " + t.length + " byte sets given)");
        }
        for (int i = 0; i < 4; ++i) {
            this.bytes[i] = Integer.parseInt(t[i]);
        }
    }
    
    public int[] getBytes() {
        return this.bytes.clone();
    }
    
    public int compareTo(final IPAddress ip) {
        final int[] bytes = ip.getBytes();
        for (int i = 0; i < bytes.length; ++i) {
            if (this.bytes[i] != bytes[i]) {
                if (this.bytes[i] > bytes[i]) {
                    return 1;
                }
                if (this.bytes[i] < bytes[i]) {
                    return -1;
                }
            }
        }
        return 0;
    }
    
    public boolean isGreaterThan(final IPAddress ip) {
        return this.compareTo(ip) == 1;
    }
    
    public boolean isLessThan(final IPAddress ip) {
        return this.compareTo(ip) == -1;
    }
    
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof IPAddress) {
            final IPAddress ip = (IPAddress)o;
            return ip.bytes.equals(this.bytes);
        }
        return false;
    }
    
    public String toString() {
        final StringBuilder sb = new StringBuilder(String.valueOf(this.bytes[0]));
        for (int i = 1; i < this.bytes.length; ++i) {
            sb.append("." + this.bytes[i]);
        }
        return sb.toString();
    }
    
    public int hashCode() {
        return Arrays.hashCode(this.bytes);
    }
}
