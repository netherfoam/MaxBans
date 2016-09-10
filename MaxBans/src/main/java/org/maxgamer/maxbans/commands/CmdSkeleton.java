package org.maxgamer.maxbans.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.util.Formatter;

public abstract class CmdSkeleton implements CommandExecutor, TabCompleter, Comparable<CmdSkeleton>
{
    private static HashSet<CmdSkeleton> commands;
    protected MaxBans plugin;
    protected String perm;
    protected int minArgs;
    protected int namePos;
    protected PluginCommand cmd;
    
    static {
        CmdSkeleton.commands = new HashSet<CmdSkeleton>();
    }
    
    public static CmdSkeleton[] getCommands() {
        return CmdSkeleton.commands.toArray(new CmdSkeleton[CmdSkeleton.commands.size()]);
    }
    
    public int compareTo(final CmdSkeleton skelly) {
        return this.cmd.getUsage().compareToIgnoreCase(skelly.cmd.getUsage());
    }
    
    public boolean equals(final Object o) {
        return o.getClass() == this.getClass();
    }
    
    public int hashCode() {
        return this.getClass().hashCode();
    }
    
    public CmdSkeleton(final String command, final String perm) {
        super();
        this.plugin = MaxBans.instance;
        this.minArgs = 0;
        this.namePos = 1;
        this.perm = perm;
        this.cmd = this.plugin.getCommand(command);
        if (this.cmd != null) {
            this.cmd.setExecutor((CommandExecutor)this);
        }
        CmdSkeleton.commands.add(this);
    }
    
    public String getUsage() {
        return Formatter.secondary + this.cmd.getUsage();
    }
    
    public String getDescription() {
        return this.cmd.getDescription();
    }
    
    public boolean hasPermission(final CommandSender sender) {
        return this.perm == null || this.perm.isEmpty() || sender.hasPermission(this.perm);
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (!this.hasPermission(sender)) {
            sender.sendMessage(Msg.get("error.no-permission"));
            return true;
        }
        if (this.minArgs > 0 && args.length < this.minArgs && this.getUsage() != null) {
            sender.sendMessage(this.getUsage());
            return true;
        }
        try {
            return this.run(sender, cmd, label, args);
        }
        catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Something went wrong when executing the command: ");
            final StringBuilder sb = new StringBuilder(args[0]);
            for (int i = 1; i < args.length; ++i) {
                sb.append(" " + args[i]);
            }
            sender.sendMessage(ChatColor.RED + "/" + label + " " + sb.toString());
            sender.sendMessage(ChatColor.RED + "Exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            sender.sendMessage(ChatColor.RED + "The remainder of the exception is in the console.");
            sender.sendMessage(ChatColor.RED + "Please report this to netherfoam: http://dev.bukkit.org/server-mods/maxbans and include the error in the console in your post.");
            return true;
        }
    }
    
    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        final ArrayList<String> results = new ArrayList<String>();
        if (args.length == this.namePos) {
            final String partial = args[this.namePos - 1];
            final String bestMatch = this.plugin.getBanManager().match(partial);
            if (bestMatch == null) {
                return results;
            }
            results.add(bestMatch);
            for (final Player p : Bukkit.getOnlinePlayers()) {
                final String name = p.getName();
                if (name.equalsIgnoreCase(bestMatch)) {
                    continue;
                }
                if (!name.startsWith(partial)) {
                    continue;
                }
                results.add(name);
            }
        }
        return results;
    }
    
    public abstract boolean run(final CommandSender p0, final Command p1, final String p2, final String[] p3);
}
