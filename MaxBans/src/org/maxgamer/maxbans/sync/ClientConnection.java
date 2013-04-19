package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;

public class ClientConnection extends Connection{
	protected static HashSet<ClientConnection> clients = new HashSet<ClientConnection>();
	private boolean authed;
	
	public ClientConnection(String host, int port, String password) throws IOException{
		this(new Socket(host, port), password);
	}
	
	/**
	 * Represents a connection that the server will have to
	 * the clients. The server will have many of these.
	 * <br/><br/>
	 * 
	 * Creates a new connection to the given address:port.
	 * This also creates a listener in another thread, which
	 * immediately tries to authenticate with the next message
	 * received.
	 */
	public ClientConnection(Socket socket, final String password){
		super(socket);
	}
	@Override
	protected boolean privateOnPacket(Packet packet){
		if(isAuthenticated() == false){ //We're not authenticated yet!
			if(packet.getCommand().equals("connect")){ //If it was a connection packet they sent
				String pass = packet.get("pass");
				if(SyncServer.getPassword().equals(pass)){ //If they sent the right password
					log(this.toString() + " authenticated.");
					Reaper.remove(this);
					setAuthenticated(); //Mark them as authenticated.
					try{print(new Packet("connect"));}catch(IOException e){}
					try{print(new Packet("msg").put("string", "Greetings, client!"));}catch(IOException e){}
					return true; //Don't let other listeners use this @connect packet.
				}
				try{close("Incorrect password");}catch(IOException e){}
				return true; //Wrong password. Don't let other listeners use this @connect packet.
			}
			else{
				try{close("Not authenticated.");}catch(IOException e){}
				return true; //Did not send a @connect request, and is NOT Authed. **DEFINITELY DON'T USE THIS PACKET!**
			}
		}
		return super.privateOnPacket(packet); //Let the other priority methods have at it.
	}
	
	@Override
	public void onClose(){
		//clients.remove(this);
	}
	
	/** Sets this connection as "Authenticated", meaning it will receive messages from the server broadcasts */
	private void setAuthenticated(){ if(authed == false) authed = true; clients.add(this); this.setMaxPacketLength(8192); }
	/** @param return true if authenticated, false if not authenticated */
	public boolean isAuthenticated(){ return authed; }
	/** The list of clients. All of these are authed. */
	public static HashSet<ClientConnection> getConnections(){ return clients; }
	
	@Override
	public void close() throws IOException{
		close("");
	}
	/** 
	 * Closes the connection.
	 * Notifies the server using @disconnect packet first.
	 * @param reason The reason for closing the connection.
	 * This reason will be sent to the remote server. 
	 */
	public synchronized void close(String reason) throws IOException{
		Packet p = new Packet("disconnect"); if(reason != null && !reason.isEmpty()) p.put("reason", reason);
		try{this.print(p);}catch(IOException e){}
		super.close();
	}
	@Override
	public void log(String s){
		SyncServer.log(s);
	}
}