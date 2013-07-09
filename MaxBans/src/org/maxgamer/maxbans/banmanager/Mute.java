package org.maxgamer.maxbans.banmanager;

public class Mute extends Punishment{
    /**
     * Creates a new mute.  Does not store it in memory or the DB
     * @param muted The player who is muted
     * @param muter The admin who muted them
     * @param time The time the mute was created
     */
	public Mute(String muted, String muter, String reason, long created){
		super(muted, reason, muter, created);
	}
}