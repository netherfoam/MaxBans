package org.maxgamer.maxbans.sync;

import org.bukkit.Bukkit;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.DNSBL;
import java.util.List;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.SyncBanManager;
import java.net.UnknownHostException;
import java.io.IOException;
import org.maxgamer.maxbans.util.OutputStreamWrapper;
import org.maxgamer.maxbans.util.InputStreamWrapper;
import java.util.LinkedList;
import java.net.Socket;
import java.util.HashMap;

public class ClientToServerConnection
{
    public static final int CONNECT_FAIL_DELAY = 5000;
    private boolean reconnect;
    private String host;
    private int port;
    private String pass;
    private HashMap<String, Command> commands;
    private Socket socket;
    private LinkedList<Packet> queue;
    private InputStreamWrapper in;
    private OutputStreamWrapper out;
    private Thread watcher;
    
    public ClientToServerConnection(final String host, final int port, final String pass) {
        super();
        this.reconnect = true;
        this.commands = new HashMap<String, Command>();
        this.queue = new LinkedList<Packet>();
        this.watcher = new Thread() {
            public void run() {
                while (ClientToServerConnection.this.reconnect) {
                    if (ClientToServerConnection.this.socket != null) {
                        try {
                            ClientToServerConnection.this.socket.close();
                        }
                        catch (IOException ex) {}
                    }
                    try {
                        ClientToServerConnection.access$4(ClientToServerConnection.this, new Socket(ClientToServerConnection.this.host, ClientToServerConnection.this.port));
                        ClientToServerConnection.access$5(ClientToServerConnection.this, new InputStreamWrapper(ClientToServerConnection.this.socket.getInputStream()));
                        ClientToServerConnection.access$6(ClientToServerConnection.this, new OutputStreamWrapper(ClientToServerConnection.this.socket.getOutputStream()));
                        Packet p = new Packet("connect").put("pass", ClientToServerConnection.this.pass);
                        ClientToServerConnection.this.write(p);
                        p = Packet.unserialize(ClientToServerConnection.this.in.readString());
                        if (!p.getCommand().equals("connect")) {
                            ClientToServerConnection.log("Server rejected connection request! Is the password correct?");
                            ClientToServerConnection.this.close();
                            continue;
                        }
                    }
                    catch (UnknownHostException e3) {
                        if (SyncUtil.isDebug()) {
                            ClientToServerConnection.log("Connection failed (UnknownHostException), retrying.");
                        }
                        try {
                            Thread.sleep(5000L);
                        }
                        catch (InterruptedException ex2) {}
                        continue;
                    }
                    catch (IOException e4) {
                        if (SyncUtil.isDebug()) {
                            ClientToServerConnection.log("Connection failed (IOException), retrying.");
                        }
                        try {
                            Thread.sleep(5000L);
                        }
                        catch (InterruptedException ex3) {}
                        continue;
                    }
                    synchronized (ClientToServerConnection.this.queue) {
                        for (final Packet p2 : ClientToServerConnection.this.queue) {
                            ClientToServerConnection.this.write(p2);
                        }
                        ClientToServerConnection.this.queue.clear();
                    }
                    // monitorexit(ClientToServerConnection.access$9(this.this$0))
                    try {
                        do {
                            final String data = ClientToServerConnection.this.in.readString();
                            Packet p;
                            try {
                                p = Packet.unserialize(data);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                ClientToServerConnection.log("Malformed packet: " + data);
                                continue;
                            }
                            try {
                                final Command c = ClientToServerConnection.this.commands.get(p.getCommand());
                                if (c == null) {
                                    ClientToServerConnection.log("Unrecognised command: '" + p.getCommand() + "'... Is this version of MaxBans up to date?");
                                }
                                else {
                                    final SyncBanManager sbm = (SyncBanManager)MaxBans.instance.getBanManager();
                                    sbm.startSync();
                                    c.run(p);
                                    sbm.stopSync();
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                ClientToServerConnection.log("Failed to handle packet!");
                            }
                        } while (!ClientToServerConnection.this.socket.isClosed());
                    }
                    catch (Exception e2) {
                        if (SyncUtil.isDebug()) {
                            e2.printStackTrace();
                        }
                        ClientToServerConnection.log("Server disconnected.  Reconnecting.");
                    }
                }
            }
        };
        this.host = host;
        this.port = port;
        this.pass = pass;
        final Command msg = new Command() {
            public void run(final Packet prop) {
                final String msg = prop.get("string");
                ClientToServerConnection.log(msg);
            }
        };
        this.commands.put("msg", msg);
        final Command announce = new Command() {
            public void run(final Packet prop) {
                final String msg = prop.get("string");
                final boolean silent = prop.has("silent");
                MaxBans.instance.getBanManager().announce(msg, silent, null);
            }
        };
        this.commands.put("announce", announce);
        final Command unwarn = new Command() {
            public void run(final Packet prop) {
                final String name = prop.get("name");
                final List<Warn> warnings = MaxBans.instance.getBanManager().getWarnings(name);
                if (warnings == null) {
                    return;
                }
                MaxBans.instance.getBanManager().deleteWarning(name, warnings.get(warnings.size() - 1));
            }
        };
        this.commands.put("unwarn", unwarn);
        final Command setip = new Command() {
            public void run(final Packet prop) {
                final String ip = prop.get("ip");
                final String name = prop.get("name");
                MaxBans.instance.getBanManager().logIP(name, ip);
            }
        };
        this.commands.put("setip", setip);
        final Command setname = new Command() {
            public void run(final Packet prop) {
                final String actual = prop.get("name");
                MaxBans.instance.getBanManager().logActual(actual, actual);
            }
        };
        this.commands.put("setname", setname);
        final Command dnsbl = new Command() {
            public void run(final Packet prop) {
                if (MaxBans.instance.getBanManager().getDNSBL() == null) {
                    return;
                }
                final String ip = prop.get("ip");
                final DNSBL.DNSStatus status = DNSBL.DNSStatus.valueOf(prop.get("status"));
                final long created = Long.parseLong(prop.get("created"));
                final DNSBL.CacheRecord r = new DNSBL.CacheRecord(status, created);
                MaxBans.instance.getBanManager().getDNSBL().setRecord(ip, r);
            }
        };
        this.commands.put("dnsbl", dnsbl);
        final Command ban = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().ban(name, reason, banner);
            }
        };
        this.commands.put("ban", ban);
        final Command ipban = new Command() {
            public void run(final Packet props) {
                final String ip = props.get("ip");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().ipban(ip, reason, banner);
            }
        };
        this.commands.put("ipban", ipban);
        final Command tempipban = new Command() {
            public void run(final Packet props) {
                final String ip = props.get("ip");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                final long expires = Long.parseLong(props.get("expires"));
                MaxBans.instance.getBanManager().tempipban(ip, reason, banner, expires);
            }
        };
        this.commands.put("tempipban", tempipban);
        final Command tempban = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                final long expires = Long.parseLong(props.get("expires"));
                MaxBans.instance.getBanManager().tempban(name, reason, banner, expires);
            }
        };
        this.commands.put("tempban", tempban);
        final Command unban = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                MaxBans.instance.getBanManager().unban(name);
            }
        };
        this.commands.put("unban", unban);
        final Command unbanip = new Command() {
            public void run(final Packet props) {
                final String ip = props.get("ip");
                MaxBans.instance.getBanManager().unbanip(ip);
            }
        };
        this.commands.put("unbanip", unbanip);
        final Command mute = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().mute(name, banner, reason);
            }
        };
        this.commands.put("mute", mute);
        final Command tempmute = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                final long expires = Long.parseLong(props.get("expires"));
                MaxBans.instance.getBanManager().tempmute(name, banner, reason, expires);
            }
        };
        this.commands.put("tempmute", tempmute);
        final Command unmute = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                MaxBans.instance.getBanManager().unmute(name);
            }
        };
        this.commands.put("unmute", unmute);
        final Command warn = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String banner = props.get("banner");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().warn(name, reason, banner);
            }
        };
        this.commands.put("warn", warn);
        final Command clearwarnings = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                MaxBans.instance.getBanManager().clearWarnings(name);
            }
        };
        this.commands.put("clearwarnings", clearwarnings);
        final Command addhistory = new Command() {
            public void run(final Packet props) {
                final String message = props.get("string");
                final String name = props.get("name");
                final String banner = props.get("banner");
                MaxBans.instance.getBanManager().addHistory(name, banner, message);
            }
        };
        this.commands.put("addhistory", addhistory);
        final Command rangeban = new Command() {
            public void run(final Packet props) {
                final String reason = props.get("reason");
                final String start = props.get("start");
                final String end = props.get("end");
                final String banner = props.get("banner");
                final long created = Long.parseLong(props.get("created"));
                final IPAddress ip1 = new IPAddress(start);
                final IPAddress ip2 = new IPAddress(end);
                final RangeBan rb = new RangeBan(banner, reason, created, ip1, ip2);
                MaxBans.instance.getBanManager().ban(rb);
            }
        };
        this.commands.put("rangeban", rangeban);
        final Command unrangeban = new Command() {
            public void run(final Packet props) {
                final String start = props.get("start");
                final String end = props.get("end");
                final IPAddress ip1 = new IPAddress(start);
                final IPAddress ip2 = new IPAddress(end);
                final RangeBan rb = new RangeBan("", "", -1L, ip1, ip2);
                MaxBans.instance.getBanManager().unban(rb);
            }
        };
        this.commands.put("unrangeban", unrangeban);
        final Command whitelist = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final boolean white = props.has("white");
                MaxBans.instance.getBanManager().setWhitelisted(name, white);
            }
        };
        this.commands.put("whitelist", whitelist);
        final Command kick = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().kick(name, reason);
            }
        };
        this.commands.put("kick", kick);
        final Command kickip = new Command() {
            public void run(final Packet props) {
                final String ip = props.get("ip");
                final String reason = props.get("reason");
                MaxBans.instance.getBanManager().kickIP(ip, reason);
            }
        };
        this.commands.put("kickip", kickip);
        final Command setimmunity = new Command() {
            public void run(final Packet props) {
                final String name = props.get("name");
                final boolean immune = props.has("immune");
                MaxBans.instance.getBanManager().setImmunity(name, immune);
            }
        };
        this.commands.put("setimmunity", setimmunity);
    }
    
    public void start() {
        if (SyncUtil.isDebug()) {
            log("Starting network listener.");
        }
        this.watcher.setDaemon(true);
        this.watcher.start();
    }
    
    public void write(final Packet p) {
        if (SyncUtil.isDebug()) {
            log("Writing packet: " + p.serialize());
        }
        try {
            this.out.write(p.serialize());
        }
        catch (Exception e) {
            if (SyncUtil.isDebug()) {
                e.printStackTrace();
                log("Queued data for transmission upon reconnection instead!");
            }
            synchronized (this.queue) {
                this.queue.addLast(p);
            }
            // monitorexit(this.queue)
        }
    }
    
    public boolean isReconnect() {
        return this.reconnect;
    }
    
    public void setReconnect(final boolean reconnect) {
        this.reconnect = reconnect;
    }
    
    public void close() {
        this.setReconnect(false);
        log("Closing connection!");
        try {
            this.socket.close();
        }
        catch (IOException ex) {}
    }
    
    public static void log(final String s) {
        Bukkit.getConsoleSender().sendMessage("[MaxBans-Syncer] " + s);
    }
    
    static /* synthetic */ void access$4(final ClientToServerConnection clientToServerConnection, final Socket socket) {
        clientToServerConnection.socket = socket;
    }
    
    static /* synthetic */ void access$5(final ClientToServerConnection clientToServerConnection, final InputStreamWrapper in) {
        clientToServerConnection.in = in;
    }
    
    static /* synthetic */ void access$6(final ClientToServerConnection clientToServerConnection, final OutputStreamWrapper out) {
        clientToServerConnection.out = out;
    }
    
    protected abstract static class Command
    {
        public abstract void run(final Packet p0);
    }
}
