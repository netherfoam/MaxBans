package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.RangeBan;
import org.maxgamer.maxbans.banmanager.SyncBanManager;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.util.DNSBL.CacheRecord;
import org.maxgamer.maxbans.util.DNSBL.DNSStatus;
import org.maxgamer.maxbans.util.IPAddress;
import org.maxgamer.maxbans.util.InputStreamWrapper;
import org.maxgamer.maxbans.util.OutputStreamWrapper;

public class ClientToServerConnection{
	/** Number of milliseconds to wait after a failed connection (Not auth!)*/
	public static final int CONNECT_FAIL_DELAY = 5000;
	
	/** True if we should reconnect, false otherwise */
	private boolean reconnect = true;
	
	private String host;
	private int port;
	private String pass;
	
	/** The map of string to commands to be executed which use packets */
	private HashMap<String, Command> commands = new HashMap<String, Command>();
	
	/** The socket connection to the remote server */
	private Socket socket;
	
	private LinkedList<Packet> queue = new LinkedList<Packet>();
	
	public ClientToServerConnection(String host, int port, String pass){
		this.host = host;
		this.port = port;
		this.pass = pass;
		/* *************************
		 * Prints the given message
		 * to the console.
		 * *************************/
		Command msg = new Command(){
			@Override
			public void run(Packet prop){
				String msg = prop.get("string");
				log(msg);
			}
		};
		commands.put("msg", msg);
		
		/* *************************
		 * Announces the given message
		 * to everyone in the server
		 * *************************/
		Command announce = new Command(){
			@Override
			public void run(Packet prop){
				String msg = prop.get("string");
				boolean silent = prop.has("silent");
				
				MaxBans.instance.getBanManager().announce(msg, silent, null);
			}
		};
		commands.put("announce", announce);
		
		/* *************************
		 * Removes a players latest
		 * warning.
		 * *************************/
		Command unwarn = new Command(){
			@Override
			public void run(Packet prop){
				String name = prop.get("name");
				List<Warn> warnings = MaxBans.instance.getBanManager().getWarnings(name);
				if(warnings == null) return;
				MaxBans.instance.getBanManager().deleteWarning(name, warnings.get(warnings.size() - 1));
			}
		};
		commands.put("unwarn", unwarn);
		
		/* *************************
		 * Logs that a player joined
		 * from the given IP address
		 * *************************/
		Command setip = new Command(){
			@Override
			public void run(Packet prop){
				String ip = prop.get("ip");
				String name = prop.get("name");
				MaxBans.instance.getBanManager().logIP(name, ip);
			}
		};
		commands.put("setip", setip);
		
		/* *************************
		 * Logs a players lowercase
		 * name VS actual case name
		 * *************************/
		Command setname = new Command(){
			@Override
			public void run(Packet prop){
				String actual = prop.get("name");
				MaxBans.instance.getBanManager().logActual(actual, actual);
			}
		};
		commands.put("setname", setname);
		
		/* *************************
		 * Logs that a given IP has
		 * a given status with the 
		 * DNSBL servers.
		 * *************************/
		Command dnsbl = new Command(){
			@Override
			public void run(Packet prop){
				if(MaxBans.instance.getBanManager().getDNSBL() == null) return;
				
				String ip = prop.get("ip");
				DNSStatus status = DNSStatus.valueOf(prop.get("status"));
				long created = Long.parseLong(prop.get("created"));
				CacheRecord r = new CacheRecord(status, created);
				MaxBans.instance.getBanManager().getDNSBL().setRecord(ip, r);
			}
		};
		commands.put("dnsbl", dnsbl);
		
		/* *************************
		 * Syncs a ban
		 * *************************/
		Command ban = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				String banner = props.get("banner");
				String reason = props.get("reason");
				
				MaxBans.instance.getBanManager().ban(name, reason, banner);
			}
		};
		commands.put("ban", ban);
		
		/* *************************
		 * Syncs an IP Ban
		 * *************************/
		Command ipban = new Command(){
			@Override
			public void run(Packet props){
				String ip = props.get("ip");
				String banner = props.get("banner");
				String reason = props.get("reason");
				
				MaxBans.instance.getBanManager().ipban(ip, reason, banner);
			}
		};
		commands.put("ipban", ipban);
		
		/* *************************
		 * Syncs a Temp IP Ban
		 * *************************/
		Command tempipban = new Command(){
			@Override
			public void run(Packet props){
				String ip = props.get("ip");
				String banner = props.get("banner");
				String reason = props.get("reason");
				long expires = Long.parseLong(props.get("expires"));
				
				MaxBans.instance.getBanManager().tempipban(ip, reason, banner, expires);
			}
		};
		commands.put("tempipban", tempipban);
		
		/* *************************
		 * Syncs a Temp Ban
		 * *************************/
		Command tempban = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				String banner = props.get("banner");
				String reason = props.get("reason");
				long expires = Long.parseLong(props.get("expires"));
				
				MaxBans.instance.getBanManager().tempban(name, reason, banner, expires);
			}
		};
		commands.put("tempban", tempban);
		
		/* *************************
		 * Syncs an unban
		 * *************************/
		Command unban = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				MaxBans.instance.getBanManager().unban(name);
			}
		};
		
		commands.put("unban", unban);
		
		/* *************************
		 * Syncs unbanning an IP
		 * *************************/
		Command unbanip = new Command(){
			@Override
			public void run(Packet props){
				String ip = props.get("ip");
				MaxBans.instance.getBanManager().unbanip(ip);
			}
		};
		
		commands.put("unbanip", unbanip);
		
		
		/* *************************
		 * Syncs a mute
		 * *************************/
		Command mute = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				String banner = props.get("banner");
				String reason = props.get("reason");
				
				MaxBans.instance.getBanManager().mute(name, banner, reason);
			}
		};
		
		commands.put("mute", mute);
		
		/* *************************
		 * Syncs a Temp Mute
		 * *************************/
		Command tempmute = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				String banner = props.get("banner");
				String reason = props.get("reason");
				long expires = Long.parseLong(props.get("expires"));
				
				MaxBans.instance.getBanManager().tempmute(name, banner, reason, expires);
			}
		};
		commands.put("tempmute", tempmute);
		
		/* *************************
		 * Syncs an unmute
		 * *************************/
		Command unmute = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				MaxBans.instance.getBanManager().unmute(name);
			}
		};
		
		commands.put("unmute", unmute);
		
		/* *************************
		 * Syncs a warning
		 * *************************/
		Command warn = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				String banner = props.get("banner");
				String reason = props.get("reason");
				
				MaxBans.instance.getBanManager().warn(name, reason, banner);
			}
		};
		commands.put("warn", warn);
		
		/* *************************
		 * Clears a players warnings.
		 * *************************/
		Command clearwarnings = new Command(){
			@Override
			public void run(Packet props){
				String name = props.get("name");
				
				MaxBans.instance.getBanManager().clearWarnings(name);
			}
		};
		commands.put("clearwarnings", clearwarnings);
		
		/* *************************
		 * Adds the given message to
		 * the message history.
		 * *************************/
		Command addhistory = new Command(){
			@Override
			public void run(Packet props){
				String message = props.get("string");
				String name = props.get("name");
				String banner = props.get("banner");
				MaxBans.instance.getBanManager().addHistory(name, banner, message);
			}
		};
		commands.put("addhistory", addhistory);
		
		/* *************************
         * Adds the given rangeban
         * *************************/
        Command rangeban = new Command(){
                @Override
                public void run(Packet props){
                        String reason = props.get("reason");
                        String start = props.get("start");
                        String end = props.get("end");
                        String banner = props.get("banner");
                        long created = Long.parseLong(props.get("created"));
                        
                        IPAddress ip1 = new IPAddress(start);
                        IPAddress ip2 = new IPAddress(end);
                        RangeBan rb = new RangeBan(banner, reason, created, ip1, ip2);
                        MaxBans.instance.getBanManager().ban(rb);
                }
        };
        commands.put("rangeban", rangeban);
        
        /* *************************
         * Removes the given rangeban
         * *************************/
        Command unrangeban = new Command(){
                @Override
                public void run(Packet props){
                        String start = props.get("start");
                        String end = props.get("end");
                        
                        IPAddress ip1 = new IPAddress(start);
                        IPAddress ip2 = new IPAddress(end);
                        RangeBan rb = new RangeBan("", "", -1, ip1, ip2);
                        MaxBans.instance.getBanManager().unban(rb);
                }
        };
        commands.put("unrangeban", unrangeban);
        
        /* *************************
         * Whitelist or unwhitelists the given user
         * *************************/
        Command whitelist = new Command(){
                @Override
                public void run(Packet props){
                        String name = props.get("name");
                        boolean white = props.has("white");
                        
                        MaxBans.instance.getBanManager().setWhitelisted(name, white);
                }
        };
        commands.put("whitelist", whitelist);
        
        /* *************************
         * Kicks anyone with the given username
         * *************************/
        Command kick = new Command(){
                @Override
                public void run(Packet props){
                        String name = props.get("name");
                        String reason = props.get("reason");
                        
                        MaxBans.instance.getBanManager().kick(name, reason);
                }
        };
        commands.put("kick", kick);
        
        /* *************************
         * Kicks anyone with the given IP Address
         * *************************/
        Command kickip = new Command(){
                @Override
                public void run(Packet props){
                        String ip = props.get("ip");
                        String reason = props.get("reason");
                        
                        MaxBans.instance.getBanManager().kickIP(ip, reason);
                }
        };
        commands.put("kickip", kickip);
        
        /* *************************
         * Kicks anyone with the given IP Address
         * *************************/
        Command setimmunity = new Command(){
                @Override
                public void run(Packet props){
                        String name = props.get("name");
                        boolean immune = props.has("immune");
                        
                        MaxBans.instance.getBanManager().setImmunity(name, immune);
                }
        };
        commands.put("setimmunity", setimmunity);
	}
	
	private InputStreamWrapper in;
	private OutputStreamWrapper out;
	
	public void start(){
		if(SyncUtil.isDebug()) log("Starting network listener.");
		watcher.setDaemon(true);
		watcher.start();
	}
	
	/** The thread that:
	 * 1. Makes the connection until successful
	 * 2. Sends queued data, if any
	 * 3. Waits for new data until close/failure
	 * 4. Returns to step 1.
	 */
	private Thread watcher = new Thread(){
		@Override
		public void run(){
			while(reconnect){
				/* 1. Attempt connection until successful
				 * 2. Send queued data, if any
				 * 3. Read data until failure
				 * 4. Go to step #1.
				 */
				//Close any previous connections
				if(socket != null){
					try {
						socket.close();
					} catch (IOException e) {}
				}
				
				//Start our new connection
				try {
					socket = new Socket(host, port);
					in = new InputStreamWrapper(socket.getInputStream());
					out = new OutputStreamWrapper(socket.getOutputStream());
					
					Packet p = new Packet("connect").put("pass", pass);
					write(p);
					//Wait for a response
					p = Packet.unserialize(in.readString());
					if(p.getCommand().equals("connect") == false){
						//Failure, incorrect password
						log("Server rejected connection request! Is the password correct?");
						close();
						continue;
					}
				} catch (UnknownHostException e1) {
					if(SyncUtil.isDebug()) log("Connection failed (UnknownHostException), retrying."); //DEBUG
					try { Thread.sleep(CONNECT_FAIL_DELAY);
					} catch (InterruptedException e) {}
					
					continue; //Retry
				} catch (IOException e1) {
					if(SyncUtil.isDebug()) log("Connection failed (IOException), retrying."); //DEBUG
					try { Thread.sleep(CONNECT_FAIL_DELAY);
					} catch (InterruptedException e) {}
					continue; //Retry.
				}
				
				//Purge the queue.
				synchronized(queue){
					for(Packet p : queue){
						write(p);
					}
					queue.clear();
				}
				
				
				//We can now read!
				try {
					Packet p;
					String data;
					while(socket.isClosed() == false){
						data = in.readString();
						try{
							p = Packet.unserialize(data);
						}
						catch(Exception e){
							e.printStackTrace();
							log("Malformed packet: " + data);
							continue; //Next input.
						}
						
						try{
							Command c = commands.get(p.getCommand());
							
							if(c == null){
								log("Unrecognised command: '" + p.getCommand() + "'... Is this version of MaxBans up to date?");
								continue; //Skip.
							}
							
							SyncBanManager sbm = (SyncBanManager) MaxBans.instance.getBanManager();
							sbm.startSync();
							c.run(p);
							sbm.stopSync();
						}
						catch(Exception e){
							e.printStackTrace();
							log("Failed to handle packet!");
						}
					}
				} catch (Exception e) { //Thrown when stream disconnects
					if(SyncUtil.isDebug()) e.printStackTrace(); //We will retry connecting.
					log("Server disconnected.  Reconnecting.");
				}
			}
		}
	};
	
	public void write(Packet p){
		if(SyncUtil.isDebug()) log("Writing packet: " + p.serialize());
		try{
			out.write(p.serialize());
		}
		catch(Exception e){
			if(SyncUtil.isDebug()){
				e.printStackTrace();
				log("Queued data for transmission upon reconnection instead!");
			}
			synchronized(queue){
				queue.addLast(p);
			}
		}
	}
	
	public boolean isReconnect(){
		return reconnect;
	}
	public void setReconnect(boolean reconnect){
		this.reconnect = reconnect;
	}
	
	public void close(){
		setReconnect(false);
		log("Closing connection!");
		try{socket.close();}
		catch(IOException e){}
	}
	
	/** Represents a command the syncer can perform */
	protected static abstract class Command{
		/** Called when this command is asked for, with the packet that requested it. This method is called in the main server thread. */
		public abstract void run(Packet packet);
	}
	
	/**
	 * Logs the given string to System.out
	 * @param s The string to log.
	 */
	public static void log(String s){
		Bukkit.getConsoleSender().sendMessage("[MaxBans-Syncer] " + s);
	}
	
}