package org.maxgamer.maxbans.util;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import java.text.ParseException;

import org.bukkit.ChatColor;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;

import java.util.HashSet;
import java.util.regex.Pattern;

public class Util
{
    //private static final String IP_REGEX = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static Pattern IP_PATTERN;
    private static Pattern VALID_CHARS_PATTERN;
    private static HashSet<String> yes;
    private static HashSet<String> no;
    
    static {
        Util.IP_PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
        Util.VALID_CHARS_PATTERN = Pattern.compile("[A-Za-z0-9_]");
        Util.yes = new HashSet<String>();
        Util.no = new HashSet<String>();
        Util.yes.add("yes");
        Util.yes.add("true");
        Util.yes.add("on");
        Util.yes.add("enable");
        Util.no.add("no");
        Util.no.add("false");
        Util.no.add("off");
        Util.no.add("disable");
    }
    
    public static boolean isIP(final String s) {
        return Util.IP_PATTERN.matcher(s).matches();
    }
    
    public static String getInvalidChars(final String s) {
        return Util.VALID_CHARS_PATTERN.matcher(s).replaceAll("");
    }
    
    public static String getShortTime(final long ms) {
        final String s = getTime(ms);
        final String[] vals = s.split(" ");
        if (vals.length < 2) {
            return s;
        }
        return String.valueOf(vals[0]) + " " + vals[1];
    }
    
    public static String getTimeUntil(long epoch) {
        epoch -= System.currentTimeMillis();
        return getTime(epoch);
    }
    
    public static String getTime(long ms) {
        ms = (long)Math.ceil(ms / 1000.0D);
        StringBuilder sb = new StringBuilder(40);
        if (ms / 31449600L > 0L) {
            final long years = ms / 31449600L;
            if (years > 100L) {
                return "Never";
            }
            sb.append(String.valueOf(years) + " "+Msg.get("times.years") + ((years == 1L) ? " " : "s "));
            ms -= years * 31449600L;
        }
        if (ms / 2620800L > 0L) {
            final long months = ms / 2620800L;
            sb.append(String.valueOf(months) + " "+Msg.get("times.months") + ((months == 1L) ? " " : "es "));
            ms -= months * 2620800L;
        }
        if (ms / 604800L > 0L) {
            final long weeks = ms / 604800L;
            sb.append(String.valueOf(weeks) + " "+Msg.get("times.weeks") + ((weeks == 1L) ? " " : "s "));
            ms -= weeks * 604800L;
        }
        if (ms / 86400L > 0L) {
            final long days = ms / 86400L;
            sb.append(String.valueOf(days) + " "+Msg.get("times.days") + ((days == 1L) ? " " : "s "));
            ms -= days * 86400L;
        }
        if (ms / 3600L > 0L) {
            final long hours = ms / 3600L;
            sb.append(String.valueOf(hours) + " "+Msg.get("times.hours") + ((hours == 1L) ? " " : "s "));
            ms -= hours * 3600L;
        }
        if (ms / 60L > 0L) {
            final long minutes = ms / 60L;
            sb.append(String.valueOf(minutes) + " "+Msg.get("times.minutes") + ((minutes == 1L) ? " " : "s "));
            ms -= minutes * 60L;
        }
        if (ms > 0L) {
            sb.append(String.valueOf(ms) + " "+Msg.get("times.seconds") + ((ms == 1L) ? " " : "s "));
        }
        if (sb.length() > 1) {
            sb.replace(sb.length() - 1, sb.length(), "");
        }
        else {
            sb = new StringBuilder("N/A");
        }
        return sb.toString();
    }
    
    public static boolean isSilent(final String[] args) {
        if (args == null) {
            return false;
        }
        for (int i = 0; i < args.length; ++i) {
            if (args[i].equalsIgnoreCase("-s")) {
                for (int j = i; j < args.length - 1; ++j) {
                    args[j] = args[j + 1];
                }
                args[args.length - 1] = "";
                return true;
            }
        }
        return false;
    }
    
    public static long getTime(final String[] args) {
        final String arg = args[2].toLowerCase();
        int modifier;
        if (Msg.get("times.hours").startsWith(arg)) {
            modifier = 3600;
        }
        else if (Msg.get("times.minutes").startsWith(arg)) {
            modifier = 60;
        }
        else if (Msg.get("times.seconds").startsWith(arg)) {
            modifier = 1;
        }
        else if (Msg.get("times.weeks").startsWith(arg)) {
            modifier = 604800;
        }
        else if (Msg.get("times.days").startsWith(arg)) {
            modifier = 86400;
        }
        else if (Msg.get("times.years").startsWith(arg)) {
            modifier = 31449600;
        }
        else if (Msg.get("times.months").startsWith(arg)) {
            modifier = 2620800;
        }
        else {
            modifier = 0;
        }
        double time = 0.0D;
        try {
            time = Double.parseDouble(args[1]);
        }
        catch (NumberFormatException ex) {}
        for (int j = 0; j < args.length - 2; ++j) {
            args[j] = args[(j + 2)];
        }
        args[args.length - 1] = "";
        args[args.length - 2] = "";
        return (long)(modifier * time) * 1000L;
    }
    
    public static String buildReason(final String[] args) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 1; i < args.length; ++i) {
            sb.append(args[i]);
            sb.append(" ");
        }
        String s = sb.toString().trim();
        s = s.replaceAll("\\\\n", "\n");
        if (s.isEmpty()) {
            return MaxBans.instance.getBanManager().defaultReason;
        }
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    
    public static boolean parseBoolean(String response) throws ParseException {
        response = response.toLowerCase();
        if (Util.yes.contains(response)) {
            return true;
        }
        if (Util.no.contains(response)) {
            return false;
        }
        throw new ParseException("Invalid boolean: " + response, 0);
    }
    
    public static String getName(final CommandSender s) {
        if (s instanceof Player) {
            return ((Player)s).getName();
        }
        return "Console";
    }
}
