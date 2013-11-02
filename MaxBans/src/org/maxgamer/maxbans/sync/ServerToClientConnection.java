package org.maxgamer.maxbans.sync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.maxgamer.maxbans.util.InputStreamWrapper;
import org.maxgamer.maxbans.util.OutputStreamWrapper;

public class ServerToClientConnection{
	private Socket socket;
	private SyncServer server;
	public ServerToClientConnection(SyncServer server, Socket s){
		this.socket = s;
		this.server = server;
	}
	
	public void start(){
		socketListener.setDaemon(true);
		socketListener.start();
	}
	
	private InputStreamWrapper in;
	private OutputStreamWrapper out;
	
	private Thread socketListener = new Thread(){
		@Override
		public void run(){
			try {
				in = new InputStreamWrapper(socket.getInputStream());
				out = new OutputStreamWrapper(socket.getOutputStream());
				if(SyncUtil.isDebug()) log("Waiting for authentication from " + ServerToClientConnection.this);
				
				try { Thread.sleep(500); } //Sleep 0.5sec - Waits for data.
				catch (InterruptedException e1) {} 
				
				String data;
				Packet p;
				
				//We want to read the string they sent us, WITHOUT blocking.
				//Because if they never terminated their string, it could be
				//an attack or dodgey protocol.
				ByteArrayOutputStream read = new ByteArrayOutputStream();
				byte b;
				while(in.available() > 0 && (b = in.readByte()) != 0){
					read.write(b);
				}
				if(SyncUtil.isDebug()) log("Read " + read.size() + " bytes of authentication.");
				try {
					data = new String(read.toByteArray(), InputStreamWrapper.CHARSET);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return; //Irrecoverable.
				}
				
				try{
					p = Packet.unserialize(data);
					if(!p.getCommand().equals("connect") || !server.getPassword().equals(p.get("pass"))){
						log(ServerToClientConnection.this + " failed to send correct password! Disconnecting.");
						close();
					}
					else{
						p = new Packet("connect");
						write(p);
						log("Connection Authenticated!");
						server.getBlacklist().remove(socket.getInetAddress().getHostAddress()); //Successful auth, so we remove any failed attempts.
					}
				}
				catch(Exception e){ //Error checking login packet, close.
					e.printStackTrace();
					log("Received malformed packet before authorising. Closing. Packet: " + data);
					socket.close();
					return; //End.
				}
				
				server.getConnections().add(ServerToClientConnection.this);
				
				if(SyncUtil.isDebug()) log("Ready for syncing!");
				while(socket.isClosed() == false){
					data = in.readString();
					try{
						p = Packet.unserialize(data);
					}
					catch(Exception e){
						e.printStackTrace();
						log("Received malformed packet: " + data);
						continue;
					}
					
					if(p.has("broadcast")){
						p.remove("broadcast");
						server.sendAll(p, ServerToClientConnection.this);
					}
				}
			} catch (IOException e) {
				log("Client disconnected.");
				if(SyncUtil.isDebug()) e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {}
			}
			if(SyncUtil.isDebug()) log("Removing connection.");
			server.getConnections().remove(ServerToClientConnection.this);
		}
	};
	
	public boolean isOpen(){
		return !socket.isClosed();
	}
	
	public void close(){
		try {
			log("Closing connection!");
			socket.close();
		} catch (IOException e) {}
	}
	public void write(Packet p){
		if(SyncUtil.isDebug()) log("Writing " + p.serialize());
		out.write(p.serialize());
	}
	
	/**
	 * Logs the given string to System.out
	 * @param s The string to log.
	 */
	public static void log(String s){
		Bukkit.getConsoleSender().sendMessage("[MaxBans-SyncServer] " + s);
	}
	
	@Override
	public String toString(){
		return "Server->Client Connection (" + socket.getInetAddress().getHostAddress() + ") " + "Open: " + (socket != null && socket.isClosed() == false && socket.isConnected());
	}
}