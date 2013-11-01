package org.maxgamer.maxbans.sync;


public class Syncer{
	private ClientToServerConnection con;
	
	public Syncer(String host, int port, String pass){
		try{
			pass = SyncUtil.encrypt(pass, SyncUtil.PASSWORD_SALT);
		}
		catch(Exception e){
			throw new RuntimeException("Failed to start Syncer: " + e.getMessage());
		}
		
		this.con = new ClientToServerConnection(host, port, pass);
	}
	
	public void start(){
		con.start();
	}
	
	public void stop(){
		con.close();
	}
	
	protected void write(Packet p){
		con.write(p);
	}
	public void broadcast(Packet p){
		p.put("broadcast", null);
		this.write(p);
	}
}