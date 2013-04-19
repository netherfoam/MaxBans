package org.maxgamer.maxbans.sync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class Connection{
	/** The socket through which to read and write data. */
	protected Socket socket;
	
	/** Whether this connection is open/able to send/receive data */
	protected volatile boolean open;
	
	/** The next connection ID */
	private static int nextId = 1;
	/** This connections ID. Every connection has an ID, authed or not */
	private int id;
	
	/** The thread which listens for data in the socket */
	private Thread listener;
	
	/** The character set used for transmitting messages. Should have 1;1 with byte[] to String. */
	public static final String CHARSET = "ISO-8859-1";
	
	/** The PacketListeners to notify when a new packet is received */
	private HashSet<PacketListener> listeners = new HashSet<PacketListener>();
	public boolean addListener(PacketListener pl){ return listeners.add(pl); }
	public boolean removeListener(PacketListener pl){ return listeners.remove(pl); }
	
	/** The maximum size for a packet */
	private int maxPacketLength = 128;
	public void setMaxPacketLength(int n){ maxPacketLength = n; }
	public int getMaxPacketLength(){ return maxPacketLength; }
	
	/** The time that this connection was created */
	private long created = System.currentTimeMillis();
	
	/**
	 * Creates a new, unauthed connection with the given
	 * host and port.
	 * @param host The host IP
	 * @param port The port
	 * @throws IOException If the socket creation throws an exception.
	 */
	public Connection(String host, int port) throws IOException{
		this(new Socket(host, port));
	}
	
	/**
	 * Creates a new unauthed connection with the given socket.
	 * @param socket The socket to run off of
	 */
	public Connection(Socket socket){
		this.socket = socket;
		this.id = nextId++;
		open = true;
		Syncer.log("Created connection #" + getID());
		listen(); //Start listening immediately, nomatter the connection.
	}
	
	/**
	 * Begins listening for data input.
	 * This method should only be called
	 * once per connection, and will only
	 * terminate when the connection is
	 * closed. It will call onPacket(Packet p);
	 * every time a packet is received, unless
	 * altered.
	 */
	public void listen(){
		if(listener != null){
			Syncer.log("Warning! Already listening!");
			return;
		}
		
		listener = new Thread(){
			@Override
			public void run(){
					while(isOpen()){
						try{
							ByteArrayOutputStream bytes = getBytes();
							if(bytes == null){
								log("Connection hard terminated by remote.");
								close();
								break; //Makes more sense than continue.
							}
							
							String responses;
							try{
								responses = new String(bytes.toByteArray(), CHARSET);
							}
							catch(UnsupportedEncodingException e){
								e.printStackTrace();
								close();
								break;
							}
							
							/* When responses are sent rapidly, they queue up. E.g. the server intends to send:
							 * @connect
							 * @msg -string Greetings, Mortal!
							 * 
							 * But instead, it is received as:
							 * @connect@msg -string Greetings, Mortal!
							 * 
							 *  Which is wrong. This corrects that.
							 */
							ArrayList<String> packs = new ArrayList<String>();
							char[] chars = responses.toCharArray();
							int start = 0;
							
							for(int i = 1; i < chars.length; i++){ //Start at 1 so we don't have an empty string at the beginning. (eg we're splitting at @ symbols, if there is an @ at the start)
								char c = chars[i];
								if(c == '\\'){
									i++; //Skip the next symbol, it is escaped. Possibly not an '@', but we don't care.
								}
								else if(c == '@'){ //Split here.
									packs.add(new String(chars, start, i - start)); //-1 because otherwise, the '@' symbol will go there.
									start = i; //restart
								}
							}
							packs.add(new String(chars, start, chars.length - start)); //This adds the last message to the list.
							
							for(String response : packs){
								Packet packet;
								try{
									packet = Packet.unserialize(response);
								}
								catch(Exception e){
									e.printStackTrace();
									log("Ignoring bad packet: '" + response + "'");
									continue;
								}
								if(privateOnPacket(packet) == false && isOpen()){
									//The packet event
									PacketEvent e = new PacketEvent(Connection.this, packet);
									
									//Each connection has a dynamic set of listeners.
									Iterator<PacketListener> it = listeners.iterator();
									while(it.hasNext()){
										PacketListener pl = it.next();
										if(pl.onPacket(e)) it.remove(); //If it returns true, it wants to be removed.
										else if(e.isHandled() == false){
											log("Unhandled packet: " + e.getPacket().serialize());
										}
										if(!isOpen()) break; //That last packet closed the connection! Stop cycling through them. (We're dead!)
									}
								}
							}
						}
						catch(IOException e){ //Thrown when the connection is closed forcefully.. E.g. remote crashes.
							try{close();}catch(IOException ex){}
						} catch (OverflowException e) {
							e.printStackTrace();
							log("Recieved too much data from " + Connection.this.toString() + ", closing connection!");
							try{close();}catch(IOException ex){}
						}
					}
				Syncer.log("Connection closed.");
				onClose();
			}
		};
		listener.start();
	}
	
	/**
	 * Returns the next set of bytes from the socket. This method blocks
	 * until data is received. Once data is received, it is returned. If
	 * the connection closes, this method returns null.
	 * @param maxLength The maximum number of bytes to read, before OverflowException is thrown.
	 * @return The bytes received, or null if the connection was terminated.
	 * @throws IOException If the socket was already closed.
	 * @throws OverflowException If the received bytes is greater than the maxLength.
	 */
	private ByteArrayOutputStream getBytes() throws IOException, OverflowException{
		InputStream in = socket.getInputStream();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(128);
		byte[] buffer = new byte[Math.min(getMaxPacketLength(), 1024)]; 
		
		while(open){
			int i = in.read(buffer);
			if(i == -1){ //End of stream here.
				return null;
			}
			bytes.write(buffer, 0, i);
			
			if(bytes.size() > maxPacketLength){ //TOO MUCH DATA RECEIVED!
				throw new OverflowException("Tried to receive over " + bytes.size() + " bytes, but max is " + maxPacketLength);
			}
			
			if(in.available() <= 0) { //End of data
				return bytes;
			}
		}
		return null;
	}
	
	/**
	 * Parses a packet privately, before onPacket is called.
	 * Use for packets like @disconnect, which should be handled
	 * first. By doing something like: <pre>
	 * 	public void privateOnPacket(Packet packet){
	 * 		//Stuff
	 * 		super.privateOnPacket(packet);
	 * 	}</pre>
	 * <br/>
	 * You are <b>guaranteed</b> to receive this packet first. It is
	 * important that you write <i>super.privateOnPacket(packet)</i>
	 * <br/>
	 * <br/>
	 * @param packet The packet
	 * @return true if the packet was handled, false if it
	 * can be safely sent to onPacket();. If true is returned, then
	 * no event will be announced to the listeners.
	 */
	protected boolean privateOnPacket(Packet packet){
		if(packet.getCommand().equals("disconnect")){
			try{
				String reason = packet.get("reason");
				Syncer.log("Remote disconnected." + (reason == null? "" : "  Reason: " + reason));
				close();
				
				if(reason.contains("Incorrect password")){
					Syncer.instance.stopReconnect();
					Syncer.instance.stop();
				}
			}
			catch(IOException e){
				e.printStackTrace();
				Syncer.log("Error closing from @disconnect request.");
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Called after this connection has been closed.
	 * By default, does nothing.
	 */
	public void onClose(){}
	
	/**
	 * Returns true if this connection is still open,
	 * and ready to read/write data to.
	 * @return true if this connection is still open,
	 * and ready to read/write data to.
	 */
	public boolean isOpen(){ return this.open; }
	/**
	 * Returns the unique ID for this connection.
	 * Every connection has an ID, authed or not.
	 * @return The unique ID.
	 */
	public int getID(){ return this.id; }
	
	/**
	 * Closes this connection. This does not notify the remote
	 * host unless it is a ClientConnection.
	 * @throws IOException If the socket is already closed.
	 */
	public synchronized void close() throws IOException{
		if(this.open){ //If we're open, close ourselves.
			this.open = false;
			
			if(this.listener != null && this.listener.isAlive()){
				this.listener.interrupt(); //Wake up the listener thread - It will terminate soon.
			}
			this.listener = null;
			//If, for some reason, this was already closed, it will throw IOException
			this.socket.close();
			//Don't call onClose. That's done by the listener when it terminates.
		}
		else{ //We were previously marked as "closed", but try to close again.
			try{
				this.socket.close(); //If this throws an exception, we were already closed
			}
			catch(IOException e){} //So die quietly.
		}
	}
	
	/**
	 * Sends the given string to the other end.
	 * @param s The bytes to send.
	 * @throws IOException If the socket is closed.
	 * If IOException is thrown, the Connection will
	 * try to close itself first, then rethrow the exception.
	 */
	private synchronized void print(byte[] bytes) throws IOException{
		socket.getOutputStream().write(bytes);
	}
	
	/**
	 * Prints the given string, with a newline character appended,
	 * to the remote address.
	 * @param s The string to print
	 * @throws IOException If the connection is closed
	 */
	public void println(String s) throws IOException{
		print(s.getBytes(CHARSET));
	}
	
	/**
	 * Sends the given packet to the remote address.
	 * @param packet
	 * @throws IOException
	 */
	public void print(Packet packet) throws IOException{
		println(packet.serialize());
	}
	
	public String toString(){ return "#" + getID() + " -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort(); }
	/**
	 * Logs the given string to System.out.
	 * @param s The string to log
	 */
	public void log(String s){
		Syncer.log(s);
	}
	
	public static abstract class PacketListener{
		/**
		 * Called when a new packet is received.
		 * @param packet The received packet.
		 * @return True if this listener should stop listening.
		 * False if it should not stop listening.<br/>
		 * <br/>
		 * (E.g. an auth listener should only listen for auth once)
		 */
		public abstract boolean onPacket(PacketEvent e);
	}
	/**
	 * Represents the receipt of a packet that is open for
	 * public accessing.
	 */
	public static class PacketEvent{
		private boolean used = false;
		public void setHandled(){ used = true; }
		public boolean isHandled(){ return used; }
		/** The packet */
		private Packet packet;
		/** The receiving connection */
		private Connection con;
		/** 
		 * @param con The connection 
		 * @param packet The packet */
		public PacketEvent(Connection con, Packet packet){
			this.con = con;
			this.packet = packet;
		}
		/** @return The received packet */
		public Packet getPacket(){ return packet; }
		/** @return The connection receiving the packet */
		public Connection getConnection(){ return con; }
	}
	/**
	 * Thrown when a connection receives too much data (E.g.
	 * a DDOS attack or something similar?).
	 */
	public static class OverflowException extends Exception{
		private static final long serialVersionUID = -3894238496324459879L;
		/** @param msg The message to be sent */
		public OverflowException(String msg){ super(msg); }
	}
	
	/**
	 * Returns true if this connection is still valid.
	 * If this connection is at any point invalid, it may
	 * be deleted by the Reaper thread.
	 * @return true if this connection is still valid.
	 */
	public boolean isValid(){
		return created + SyncServer.TIMEOUT > System.currentTimeMillis() && open;
	}
	
	/** Returns the epoch time that this connection was created. See System#currentTimeMillis() */
	public long getCreated(){ return created; }
}