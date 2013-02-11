package org.maxgamer.maxbans.sync;

import java.io.IOException;

import org.maxgamer.maxbans.MaxBans;

public class ServerConnection extends Connection{
	/** If connection is lost, should we try to reconnect? */
	private boolean reconnect = true;
	
	/**
	 * Creates a new ServerConnection.  A ServerConnection does
	 * not require the server to supply a password. 
	 * @param host The remote address
	 * @param port The remote port
	 * @throws IOException If the socket could not be created.
	 */
	public ServerConnection(String host, int port) throws IOException{
		super(host, port);
		setMaxPacketLength(8192);
	}
	
	/** Stops this connection from reconnecting if it dies */
	public void stopReconnect(){ reconnect = false; }
	
	/**
	 * Attempts to reconnect to the server, unless stopReconnect();
	 * has been called - In which case, does nothing.
	 */
	@Override
	public void onClose(){
		log("ServerConnection was closed...");
		
		if(reconnect == false) return;
		MaxBans.instance.getSyncer().stop();
		MaxBans.instance.getSyncer().start();
	}
	
	/**
	 * Tries to authenticate with the server with the given password.
	 * @param password The password 
	 * @throws IOException If the connection is closed already.
	 * If the server declines the password, this connection closes itself.
	 */
	public void connect(String password) throws IOException{
		//Listen from a response from the server
		this.addListener(new PacketListener(){
			@Override
			public boolean onPacket(PacketEvent e){
				Packet packet = e.getPacket();
				if(packet.getCommand().equals("connect")){
					e.setHandled();
					log("Server accepted!");
					MaxBans.instance.getSyncer().onAuth();
					return true;
				}
				return false;
			}
		});
		//Send password to server
		print(new Packet().setCommand("connect").put("pass", password));
	}
}