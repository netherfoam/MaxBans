package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.maxgamer.maxbans.sync.Connection.PacketEvent;
import org.maxgamer.maxbans.sync.Connection.PacketListener;
import org.maxgamer.maxbans.util.Util;

public class SyncServer{
	//Static Variables
	/** The desired interval between pings */
	public static final int PING_INTERVAL = 5000; //Ping every PING_TIME ms.
	/** The time allowed for responses (Such as Auth'ing) before giving up */
	public static final int TIMEOUT = 5000; //Time to give server/etc to respond before giving up.
	
	/** The server socket. */
	private ServerSocket server;
	/** The password for the server */
	private static String password;
	/** @return The password for the server */
	public static String getPassword(){return password;}
	
	/** A blacklist of IP, FailedConnectionAttempts */
	private volatile HashMap<String, Integer> blacklist = new HashMap<String, Integer>();
	
	/**
	 * Creates a standalone SyncServer, which does not
	 * require Bukkit to be running. Accepts some basic
	 * commands, but nothing fancy. This is preferable
	 * to Bukkit, but not as feasible for some servers.
	 */
	public static void main(String[] args){
		Integer port = null;
		String pass = null;
		
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			
			if(args.length > i+1){
				if(arg.equalsIgnoreCase("-pass")){
					pass = args[++i];
					continue;
				}
				else if(arg.equalsIgnoreCase("-port")){
					String pString = args[++i];
					try{
						port = Integer.parseInt(pString);
						if(port <= 0 || port >= 65535) throw new NumberFormatException("Port must be between 1 and 65535!");
					}
					catch(NumberFormatException e){
						System.out.println("Port is invalid: " + pString);
						return;
					}
					continue;
				}
			}
			
			if(pass == null) pass = args[i];
			else if(port == null){
				try{
					port = Integer.parseInt(arg);
					if(port <= 0 || port >= 65535) throw new NumberFormatException("Port must be between 1 and 65535!");
				}
				catch(NumberFormatException e){
					System.out.println("Port is invalid: " + arg);
					return;
				}	
			}
		}
		
		if(pass == null){
			log("Invalid arguments (No password). Usage: java -jar MaxBans.jar -pass PASSWD -port PORT");
			return;
		}
		if(port == null){
			port = Integer.valueOf(2711);
		}
		
		try{
			log("Starting server. Pass: " + pass + ", Port: " + port);
			final SyncServer server = new SyncServer(port, pass);
			
			new Thread(){
				@Override
				public void run(){
					Scanner input = new Scanner(System.in);
					while(input.hasNextLine()){
						String line = input.nextLine().toLowerCase();
						
						if(line.equals("stop") || line.equals("exit")){
							log("Close requested...");
							server.stop();
							break;
						}
						else if(line.equals("status")){
							log("Port: " + server.server.getLocalPort() + ", Pass: " + SyncServer.password);
							log("Connections: " + ClientConnection.getConnections().size());
							
							log("#ID -> Address:Port -> Time Connected");
							for(Connection con : ClientConnection.getConnections()){
								log(con.toString() + " -> " + Util.getTime(System.currentTimeMillis() - con.getCreated()));
							}
						}
						else if(line.startsWith("kill ")){
							Scanner sc = new Scanner(line);
							sc.next(); //"kill"
							if(sc.hasNextInt()){
								int id = sc.nextInt();
								
								ClientConnection cc = null;
								for(ClientConnection con : ClientConnection.getConnections()){
									if(con.getID() == id) cc = con;
								}
								if(cc == null){
									log("No connection #" + id);
								}
								else{
									try{
										ClientConnection.getConnections().remove(cc);
										cc.open = false;
										cc.socket.close();
										log("Success. Killed #" + id + " without notifying.");
									}
									catch(IOException e){
										log("Failed to kill #" + id + ". Is it closed already?");
									}
								}
							}
							else{
								log("No connection # given.");
							}
						}
						else if(line.startsWith("close ")){
							Scanner sc = new Scanner(line);
							sc.next(); //"kill"
							if(sc.hasNextInt()){
								int id = sc.nextInt();
								
								ClientConnection cc = null;
								for(ClientConnection con : ClientConnection.getConnections()){
									if(con.getID() == id) cc = con;
								}
								if(cc == null){
									log("No connection #" + id);
								}
								else{
									try{
										ClientConnection.getConnections().remove(cc);
										cc.close("Server Console requested to close.");
										log("Success. Closed #" + id + " without notifying.");
									}
									catch(IOException e){
										log("Failed to close  #" + id + ". Is it closed already?");
									}
								}
							}
							else{
								log("No connection # given.");
							}
						}
						else if(line.isEmpty()){
							log("No command given.");
						}
						else{
							log("--- Available Commands ---");
							log("stop: Stops the MaxBans SyncServer");
							log("status: Prints info about the MaxBans SyncServer");
							log("kill <id>: Kills the given connection ID without notifying. (As if server crashed)");
							log("close <id>: Closes the given connection and notifies. (As if server said goodbye)");
						}
					}
				}
			}.start();
		}
		catch(IOException e){
			e.printStackTrace();
			log("Error running server!");
			System.exit(1);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			log("Could not encrypt SyncServer password!");
		}
	}
	
	/**
	 * Creates a new server object. This creates a thread
	 * which listens for connections. Each connection is
	 * given its own thread.
	 * @param port The port to bind
	 * @param password The password required to connect
	 * @throws IOException If the socket could not be created
	 * @throws NoSuchAlgorithmException If the password could
	 * not be encrypted
	 */
	public SyncServer(int port, String password) throws IOException, NoSuchAlgorithmException{
		SyncServer.password = SyncUtil.encrypt(password, SyncUtil.PASSWORD_SALT);
		log("Encrypted pass: " + SyncServer.password);
		//Phase 1: Start the server itself.
		server = new ServerSocket(port);
		log("Server started on localhost:"+server.getLocalPort());
		
		//Phase 2: Start waiting for connections in a seperate thread.
		new Thread(){
			@Override
			public void run(){
				try {
					//While the server is still running
					while(!server.isClosed()){
						//Wait for the next connection
						Socket socket = server.accept();
						
						//Blacklist - Untested.
						synchronized(blacklist){
							Integer val = blacklist.get(socket.getInetAddress().getHostAddress());
							int attempts = 0;
							if(val != null) attempts = val;
							
							blacklist.put(socket.getInetAddress().getHostAddress(), attempts + 1);
							if(attempts > 5){
								if(attempts == 6) log(socket.getInetAddress().getHostAddress() + " is now blacklisted after " + attempts + " failed attempts to connect.");
								socket.close();
								continue;
							}
						}
						//End blacklist.
						
						//Create them as a client. This authenticates
						//them or destroys them as necessary.
						PacketListener pl = new PacketListener() {
							@Override
							public boolean onPacket(PacketEvent e) {
								if(e.getPacket().has("broadcast")){
									e.getPacket().remove("broadcast");
									broadcast(e.getPacket().serialize(), (ClientConnection) e.getConnection());
									e.setHandled();
								}
								return false;
							}
						};
						ClientConnection cc = new ClientConnection(socket, SyncServer.password);
						Reaper.add(cc);
						cc.addListener(pl);
					}
				}
				catch(SocketException e){} //This means that the server socket was closed. This is fine.
				catch (IOException e) {
					e.printStackTrace();
					log("What happened?!");
				}
			}
		}.start();
	}
	
	/** Stops the server and all active connections. */
	public void stop(){
		Iterator<ClientConnection> cit = ClientConnection.getConnections().iterator();
		while(cit.hasNext()){
			ClientConnection con = cit.next();
			try{
				con.close("Server shutting down.");
				cit.remove();
			}
			catch(IOException e){
				e.printStackTrace();
				log("SyncServer could not close ClientConnection #" + con.getID());
			}
		}
		try{
			this.server.close();
		}
		catch(IOException e){
			e.printStackTrace();
			log("Could not close server socket.");
		}
	}
	
	/** Broadcasts the given string to everyone except the given client (Eg a sender) */
	public void broadcast(String message, ClientConnection exclude){
		for(ClientConnection con : ClientConnection.getConnections()){
			if(con == exclude) continue;
			try {
				con.println(message);
			} catch (IOException e) {
				e.printStackTrace();
				log("Could not broadcast to: " + con.toString());
			}
		}
	}
	
	public static void log(String s){
		System.out.println("[MaxBans-SyncServer] " + s);
	}
}