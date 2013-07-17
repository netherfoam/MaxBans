package org.maxgamer.maxbans.sync;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
public class Reaper{
	/** The HashSet of connections to watch */
	private static HashSet<Connection> connections = new HashSet<Connection>();
	/** The thread that watches */
	private static Thread thread;
	
	/**
	 * Adds the given connection to the watchlist. If, when the reaper scans,
	 * the connection returns false for {@link Connection#isValid()}, the 
	 * connection will be "reaped" and closed.
	 * @param connection The connection to watch
	 * @return False if the connection was already being watched. True if
	 * it was not being watched yet.
	 */
	public static boolean add(Connection connection){
		boolean result = connections.add(connection); //Add it to the connections BEFORE starting the reaper
		if(thread == null || !thread.isAlive()) start(); //Start the reaper after. Otherwise, reaper will immediately exit.
		return result;
	}
	
	/**
	 * Removes the given connection from the watchlist. This means that
	 * the connection will not be closed if it returns false for
	 * {@link Connection#isValid()}.
	 * @param connection The connection to stop reaping
	 * @return True if the connection was being watched. False if it was 
	 * not being watched.
	 */
	public static boolean remove(Connection connection){
		return connections.remove(connection); //Thread terminates itself when there is no connections left to reap.
	}
	
	/**
	 * Starts the reaper thread.  The reaper thread will terminate itself
	 * if it has no connections to reap.  This method is called whenever
	 * the reaper has a connection added, and the list of connections was
	 * empty.
	 */
	private static void start(){
		if(thread != null && thread.isAlive()){
			log("Reaper could not be started - Already running!");
			return;
		}
		
		thread = new Thread(){
			@Override
			public void run(){
				while(!connections.isEmpty()){ 
					reap();
					try{ Thread.sleep(SyncServer.TIMEOUT); }
					catch(InterruptedException e){}
				}
			}
		};
		thread.start();
	}
	/** Checks and destroys any invalid connections */
	private static void reap(){
		Iterator<Connection> it = connections.iterator();
		while(it.hasNext()){
			Connection con = it.next();
			try{
				if(con.isValid() == false){
					it.remove(); 
					con.close();
				}
				}
			catch(IOException e){ log("Reaper failed to close connection!"); }
		}
	}
	public static void log(String s){
		System.out.println("[MaxBans-Reaper] " + s);
	}
}