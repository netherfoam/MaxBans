package org.maxgamer.maxbans.util;

import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.banmanager.Punishment;

/**
 * Represents a ban on a set of IPs.  For example, a
 * rangeban may cover IPs 192.168.2.1-192.168.2.255, which
 * means that nobody may connect if their IP matches 192.168.2.x
 * @author netherfoam
 *
 */
public class RangeBan extends Punishment implements Comparable<RangeBan>{
	private IPAddress start;
	private IPAddress end;
	
	public RangeBan(String banner, String reason, long created, IPAddress start, IPAddress end){
		super(start + "-" + end, reason, banner, created);
		if(start.compareTo(end) > 0){ //Start is smaller than end
			//The given addresses need to be swapped.
			this.start = end; this.end = start;
		}
		else{
			//The given IPs are fine.
			this.start = start; this.end = end;
		}
	}
	/**
	 * The start of this RangeBan
	 * @return The start of this RangeBan
	 */
	public IPAddress getStart(){
		return start;
	}
	/**
	 * The end of this RangeBan
	 * @return The end of this RangeBan
	 */
	public IPAddress getEnd(){
		return end;
	}
	
	/**
	 * Returns the value of this start.compareTo(theOther.start)
	 */
	public int compareTo(RangeBan rb) {
		return start.compareTo(rb.start); //Actually just compare the start values of both ranges.
	}
	
	@Override
	public int hashCode(){
		return start.hashCode();
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof RangeBan){
			RangeBan rb = (RangeBan) o;
			return start.equals(rb.start) && end.equals(rb.end);
		}
		return false;
	}
	@Override
	public String toString(){
		return start.toString() + "-" + end.toString();
	}
	/**
	 * Returns true if the given RangeBan overlaps this one or visa versa
	 * @param rb The rangeban to check
	 * @return True if they overlap.
	 */
	public boolean overlaps(RangeBan rb){
		if(start.compareTo(rb.end) == end.compareTo(rb.start)){
			//If this one starts after the other one ends, OR this one ends before the other one starts...
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true if the given IP address is contained by this rangeban.
	 * @param address The address
	 * @return True if it is contained
	 */
	public boolean contains(IPAddress address){
		return address.compareTo(start) >= 0 && address.compareTo(end) <= 0;
	}
	
	/**
	 * You're banned!<br/>
	 * Reason: 'Misconduct'<br/>
	 * By Console.
	 */
	public String getKickMessage(){
		StringBuilder sb = new StringBuilder(50);
		sb.append(Formatter.regular + "Your IP Address (" + Formatter.secondary + this.toString() + Formatter.regular + ") is RangeBanned.\n");
		sb.append(Formatter.regular + "Reason: " + Formatter.reason + this.getReason() + "\n");
		sb.append(Formatter.regular + "By: " + Formatter.banner + this.getBanner());
		
		 //Append the appeal message, if necessary.
        String appeal = MaxBans.instance.getBanManager().getAppealMessage();
        if(appeal != null && appeal.isEmpty() == false){
        	sb.append("\n" + Formatter.regular + appeal);
        }
        return sb.toString();
	}
}