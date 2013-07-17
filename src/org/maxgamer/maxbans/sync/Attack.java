package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import org.maxgamer.maxbans.sync.Connection.PacketEvent;
import org.maxgamer.maxbans.sync.Connection.PacketListener;

/**
 * A client designed to attack the given server.
 *<br/><br/>
 *This client allows you to send any Packet object, and
 *prints out any received packets.
 *
 *This is used for debugging purposes only.
 */
public class Attack{
	public static String password;
	public static void main(String[] args){
		Integer port = null;
		String host = null;
		
		for(int i = 0; i < args.length; i++){
			String arg = args[i];
			
			if(args.length > i+1){
				if(arg.equalsIgnoreCase("-host")){
					host = args[++i];
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
			
			if(host == null) host = args[i];
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
		
		if(host == null){
			log("Invalid arguments (No host). Usage: java -jar MaxBans.jar -host HOST -port PORT");
			return;
		}
		if(port == null){
			port = Integer.valueOf(2711);
		}
		
		new Attack(host, port);
	}
	public static void log(String s){
		System.out.println("[MaxBans-Attack] " + s);
	}
	
	//Dynamic vars
	private Connection con;
	public Attack(final String host, final int port){
		try{
			PacketListener pl = new PacketListener() {
				@Override
				public boolean onPacket(PacketEvent e) {
					log("Received: " + e.getPacket().serialize());
					return false;
				}
			};
			con = new Connection(host, port){
				@Override
				public boolean privateOnPacket(Packet packet){return false;} //Don't listen to disconnect packets! I want ALL DATA! Evil!
			};
			
			con.addListener(pl);
		}
		catch(IOException e){
			e.printStackTrace();
			log("Could not start connection.");
		}
		
		new Thread(){
			@Override
			public void run(){
				Scanner sc = new Scanner(System.in);
				while(sc.hasNextLine()){
					String line = sc.nextLine();
					String[] args = line.split(" ");
					args[0] = args[0].toLowerCase();
					try{
						if(args[0].equals("attack")){
							int size = Integer.parseInt(args[1]);
							String crap = SyncUtil.getRandomString(size);
							try{
								con.print(new Packet().setCommand(crap));
							}
							catch(SocketException se){
								log("Failed to send. Reconnecting...");
								con = new Connection(host, port);
								con.print(new Packet("connect").put("pass", password));
								con.print(new Packet().setCommand(crap));
							}
						}
						else if(args[0].equals("send")){
							line = line.replaceFirst("send ", "");
							try{
								con.println(line);
							}
							catch(SocketException se){
								log("Failed to send. Reconnecting...");
								con = new Connection(host, port);
								con.print(new Packet("connect").put("pass", password));
								con.println(line);
							}
						}
						else if(args[0].equals("connect")){
							line = line.replaceFirst("connect ", "");
							try{
								password = SyncUtil.encrypt(line, SyncUtil.PASSWORD_SALT);
								con.print(new Packet("connect").put("pass", password)); }
							catch(NoSuchAlgorithmException e){ e.printStackTrace(); }
						}
						else if(args[0].equals("stop")){
							log("Stopping...");
							con.close();
							return;
						}
						else{
							log("Available commands: ");
							log("attack <size>: Sends garbage to the remote server of <size> bytes.");
							log("send <packet>: Sends the given packet. E.g. @connect -pass MyPassWord");
							log("stop: Stops the attack client nicely.");
						}
					}
					catch(IndexOutOfBoundsException e){
						e.printStackTrace();
						log("Not enough args.");
					}
					catch(NumberFormatException e){
						e.printStackTrace();
						log("Not a number: '" + e.getMessage() + "'");
					} catch (IOException e) {
						e.printStackTrace();
						log("Could not send data!");
					}
				}
			}
		}.start();
	}
}