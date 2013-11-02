package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Bukkit;

public class SyncServer{
	public final int MAX_FAILED_AUTH_ATTEMPTS = 10;
	private int port;
	private String pass;
	private HashSet<ServerToClientConnection> connections = new HashSet<ServerToClientConnection>();
	private HashMap<String, Integer> blacklist = new HashMap<String, Integer>();
	
	/**
	 * Fetches the IP Blacklist.
	 * @return A map of String(IP) to Integer(Failed attempts).
	 * The value in this should be reset on a successful authentication.
	 */
	public HashMap<String, Integer> getBlacklist(){ return blacklist; }
	public HashSet<ServerToClientConnection> getConnections(){ return connections; }
	
	ServerSocket core;
	private Thread watcher = new Thread(){
		@Override
		public void run(){
			while(core.isClosed() == false){
				try{
					Socket s = core.accept();
					
					//Blacklist handling.
					//If a user gets X marks in the blacklist, then they should be denied from connecting.
					String ip = s.getInetAddress().getHostAddress();
					Integer i = blacklist.get(ip);
					if(i == null) i = 1;
					else i++;
					blacklist.put(ip, i);
					
					if(i >= MAX_FAILED_AUTH_ATTEMPTS){
						if(SyncUtil.isDebug()) log("Connection from " + ip + " denied - Too many failed authentication attempts (" + i + ").");
						s.close();
						continue; //This IP is blacklisted.
					}
					
					if(SyncUtil.isDebug()) log("Connection request from " + s.getInetAddress().getHostAddress());
					ServerToClientConnection con = new ServerToClientConnection(SyncServer.this, s);
					con.start();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
	};
	
	public SyncServer(int port, String pass){
		this.port = port;
		try {
			this.pass = SyncUtil.encrypt(pass, SyncUtil.PASSWORD_SALT);
		} catch (Exception e){
			throw new RuntimeException("Failed to encrypt password: " + e.getMessage());
		}
	}
	
	public void start() throws IOException{
		if(SyncUtil.isDebug()) log("Starting server.");
		core = new ServerSocket(port);
		watcher.setDaemon(true);
		watcher.start();
		if(SyncUtil.isDebug()) log("Server started successfully.");
	}
	
	public void stop(){
		try { core.close(); } 
		catch (IOException e) {}
	}
	
	public void sendAll(Packet p, ServerToClientConnection except){
		Iterator<ServerToClientConnection> sit = connections.iterator();
		while(sit.hasNext()){
			ServerToClientConnection con = sit.next();
			if(con == except){
				continue;
			}
			if(con.isOpen() == false){
				sit.remove();
				continue; //Dead connection.
			}
			
			try{
				con.write(p);
			}
			catch(Exception e){
				if(SyncUtil.isDebug()) e.printStackTrace();
				if(SyncUtil.isDebug()) log("Failed to send data to client.");
				//Close it, remove it.
				con.close();
				sit.remove();
			}
		}
	}
	
	public String getPassword(){
		return pass;
	}
	
	
	/**
	 * Logs the given string to System.out
	 * @param s The string to log.
	 */
	public static void log(String s){
		Bukkit.getConsoleSender().sendMessage("[MaxBans-SyncServer] " + s);
	}
}