package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Warn;
import org.maxgamer.maxbans.sync.Connection.PacketEvent;
import org.maxgamer.maxbans.sync.Connection.PacketListener;
import org.maxgamer.maxbans.util.DNSBL.CacheRecord;
import org.maxgamer.maxbans.util.DNSBL.DNSStatus;

public class Syncer{
	private HashMap<String, Command> commands = new HashMap<String, Command>();
	private ServerConnection server;
	
	private String host;
	private int port;
	private String password;
	
	private LinkedList<Packet> queue = new LinkedList<Packet>();
	
	public static Syncer instance;
	
	/** Stops the Syncer from reconnecting */
	public void stopReconnect(){ if(server != null) server.stopReconnect(); }
	
	public Syncer(String host, int port, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		instance = this;
		this.host = host;
		this.port = port;
		this.password = SyncUtil.encrypt(password, SyncUtil.PASSWORD_SALT);
		
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
				
				MaxBans.instance.getBanManager().mute(name, banner);
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
				long expires = Long.parseLong(props.get("expires"));
				
				MaxBans.instance.getBanManager().tempmute(name, banner, expires);
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
	}
	
	/**
	 * Tries to connect once.
	 * @throws UnknownHostException If the host was not reached
	 * @throws IOException If the socket throws this exception
	 */
	public void connect() throws UnknownHostException, IOException{
		this.server = new ServerConnection(host, port);
		this.server.connect(password);
		PacketListener pl = new PacketListener(){
			@Override
			public boolean onPacket(final PacketEvent e) {
				final Command c = commands.get(e.getPacket().getCommand());
				if(c != null){
					Bukkit.getScheduler().scheduleSyncDelayedTask(MaxBans.instance, new Runnable(){ //Run it in the main thread
						@Override
						public void run(){
							c.run(e.getPacket());
						}
					});
					e.setHandled();
				}
				return false; //Never stop listening.
			}
		};
		this.server.addListener(pl);
	}
	
	/** Called when the server connection is accepted */
	public void onAuth(){
		if(queue.isEmpty()) return;
		log("Authenticated! Sending " + queue.size() + " old packets!");
		while(!queue.isEmpty()){
			Packet packet = queue.remove();
			send(packet);
			try{ Thread.sleep(10); } catch(InterruptedException e){}
		}
	}
	
	/** Attempts to start the syncer, until the stop method is called. */
	public void start(){
		log("Starting...");
		Runnable r = new Runnable(){
			int tries = 0;
			final int MAX_TRIES = (MaxBans.instance.getConfig().getInt("sync.tries", 120)); 	//120 tries by default, configurable for more/less.
			final int DELAY_TICKS = SyncServer.PING_INTERVAL / 50; 		//5000ms/50 = 100 ticks. Bukkit uses ticks.  = 5 secs.
			//= 10 mins of trying, or give up.
			
			public void run(){
				try{
					tries++;
					connect();
				}
				catch(ConnectException e){
					if(tries < MAX_TRIES){
						if(MaxBans.instance == null) return; //Plugin disabled
						Bukkit.getScheduler().runTaskLaterAsynchronously(MaxBans.instance, this, DELAY_TICKS);
					}
					else{
						MaxBans.instance.getLogger().info("Could not connect after " + tries * DELAY_TICKS / 1200.0 + " minutes. Giving up. Disabling syncer til reboot.");
					}
					
				}
				catch(UnknownHostException e){ //Thrown when connecting to invalid IP address.
					e.printStackTrace();
					MaxBans.instance.getLogger().info("Connection failed: " + host + ":" + port + ". Disabling syncer til reboot.");
				}
				catch(IOException e){
					e.printStackTrace();
					MaxBans.instance.getLogger().info("Could not use socket. Disabling syncer til reboot.");
					stopReconnect();
					stop();
				}
			}
		};
		r.run();
	}
	
	/**
	 * Stops this Sync listener safely.
	 * Informs the SyncServer that this is
	 * shutting down.
	 */
	public void stop(){
		log("Stopping syncer.");
		try {
			if(this.server != null){
				if(this.server.isOpen()) this.server.print(new Packet().setCommand("disconnect").put("reason", "Syncer being shutdown"));
				this.server.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			log("Could not close connection to server! Possibly already closed?");
		} catch(Exception e){
			e.printStackTrace();
			log("Something went wrong using Syncer.stop()!");
		}
	}
	
	/**
	 * Requests the primary server to broadcast the given packet.
	 * @param properties The packet.
	 */
	public void broadcast(Packet properties){
		properties.put("broadcast", null);
		send(properties);
	}
	
	/**
	 * Sends the given packet in a new thread.
	 * @param properties The packet.
	 */
	public void send(final Packet properties){
		new Thread(){
			@Override
			public void run(){
				try {
					if(server == null || !server.isOpen()){
						queue.add(properties);
						log("Could not send request to SyncServer (Not connected). Queued it instead.");
					}
					else{
						server.print(properties);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}.start();
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
		System.out.println("[MaxBans-Sync] " + s);
	}
}