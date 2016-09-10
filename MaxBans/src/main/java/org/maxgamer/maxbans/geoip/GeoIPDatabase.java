package org.maxgamer.maxbans.geoip;

import org.maxgamer.maxbans.util.IPAddress;
import java.util.regex.Matcher;
import java.util.LinkedList;
import java.util.regex.Pattern;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.File;
import java.util.TreeSet;

public class GeoIPDatabase
{
    public static final byte IP_VALUE_POS = 0;
    public static final byte IP_COUNTRY_CODE_POS = 1;
    public static final byte IP_COUNTRY_POS = 1;
    private TreeSet<GeoIP> info;
    private File file;
    
    public void reload() {
        this.info = new TreeSet<GeoIP>();
        if (this.file == null || !this.file.exists()) {
            System.out.println("WARNING: Could not load GeoIPDatabase. The file does not exist.");
            return;
        }
        Scanner sc;
        try {
            sc = new Scanner(this.file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        final Pattern pattern = Pattern.compile("(\"[^\"]*\")|([^\\,]+)");
        int line = 0;
        while (sc.hasNextLine()) {
            ++line;
            try {
                final String s = sc.nextLine();
                final Matcher matcher = pattern.matcher(s);
                final LinkedList<String> strs = new LinkedList<String>();
                while (matcher.find()) {
                    strs.add(matcher.group());
                }
                final String[] p = strs.toArray(new String[strs.size()]);
                for (int i = 0; i < p.length; ++i) {
                    int start = 0;
                    int end = p[i].length();
                    if (p[i].endsWith("\"")) {
                        --end;
                    }
                    if (p[i].startsWith("\"")) {
                        ++start;
                    }
                    p[i] = p[i].substring(start, end);
                }
                final GeoIP ip = new GeoIP(Long.parseLong(p[0]), p[1]);
                this.info.add(ip);
            }
            catch (Exception e2) {
                System.out.println("Error on line #" + line + " of GeoIPDatabase.");
                e2.printStackTrace();
            }
        }
        sc.close();
    }
    
    public GeoIPDatabase(final File csv) {
        super();
        this.file = csv;
        this.reload();
    }
    
    public String getCountry(final String ip) {
        final IPAddress data = new IPAddress(ip);
        final long value = this.getCode(data);
        final GeoIP dummy = new GeoIP(value, "dummy");
        final GeoIP result = this.info.floor(dummy);
        if (result == null) {
            return "NULL";
        }
        return result.getCountry();
    }
    
    private long getCode(final IPAddress data) {
        final int[] bytes = data.getBytes();
        long value = 0L;
        value += 16777216L * bytes[0];
        value += 65536L * bytes[1];
        value += 256L * bytes[2];
        value += bytes[3];
        return value;
    }
}
