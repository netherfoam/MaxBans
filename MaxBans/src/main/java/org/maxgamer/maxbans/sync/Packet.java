package org.maxgamer.maxbans.sync;

import java.util.HashMap;
import java.util.Map;

public class Packet
{
    private String command;
    private HashMap<String, String> values;
    private static String[] escapers;
    private static String[] unescapers;
    
    static {
        Packet.escapers = new String[] { "-", "\\n", "@" };
        Packet.unescapers = new String[] { "\\-", "\\\\n", "\\@" };
    }
    
    public Packet() {
        super();
        this.values = new HashMap<String, String>();
    }
    
    public Packet(final String command) {
        super();
        this.values = new HashMap<String, String>();
        this.command = command;
    }
    
    public Packet put(final String key, final String value) {
        this.values.put(key, value);
        return this;
    }
    
    public Packet remove(final String key) {
        this.values.remove(key);
        return this;
    }
    
    public Packet put(final String key, final Object value) {
        this.put(key, String.valueOf(value));
        return this;
    }
    
    public String get(final String key) {
        return this.values.get(key);
    }
    
    public String getCommand() {
        return this.command;
    }
    
    public boolean has(final String key) {
        return this.values.containsKey(key);
    }
    
    public Packet setCommand(final String type) {
        this.command = type;
        return this;
    }
    
    public HashMap<String, String> getProperties() {
        return this.values;
    }
    
    public String serialize() {
        final StringBuilder sb = new StringBuilder("@").append(this.getCommand());
        for (final Map.Entry<String, String> entry : this.values.entrySet()) {
            sb.append(" -" + entry.getKey());
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(" ").append(escape(entry.getValue()));
            }
        }
        return sb.toString();
    }
    
    public static Packet unserialize(final String serial) {
        final Packet prop = new Packet();
        final String[] parts = serial.split(" -");
        prop.setCommand(parts[0].substring(1));
        for (int i = 1; i < parts.length; ++i) {
            final String part = parts[i];
            final String[] pieces = part.split(" ");
            final String key = pieces[0];
            final StringBuilder value = new StringBuilder();
            if (pieces.length > 1) {
                value.append(pieces[1]);
                for (int j = 2; j < pieces.length; ++j) {
                    value.append(" ").append(pieces[j]);
                }
            }
            prop.put(key, unescape(value.toString()));
        }
        return prop;
    }
    
    private static String escape(String s) {
        for (int i = 0; i < Packet.escapers.length; ++i) {
            s = s.replace(Packet.escapers[i], Packet.unescapers[i]);
        }
        return s;
    }
    
    private static String unescape(String s) {
        for (int i = 0; i < Packet.escapers.length; ++i) {
            s = s.replace(Packet.unescapers[i], Packet.escapers[i]);
        }
        return s;
    }
    
    public boolean equals(final Object o) {
        if (o != null && o instanceof Packet) {
            final Packet p = (Packet)o;
            if (p.getProperties().equals(this.getProperties()) && p.getCommand().equals(this.getCommand())) {
                return true;
            }
        }
        return false;
    }
}
