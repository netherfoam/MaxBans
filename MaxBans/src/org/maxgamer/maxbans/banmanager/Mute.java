package org.maxgamer.maxbans.banmanager;

public class Mute{
    private String muter;
    private long time;
    
    /**
     * Creates a new mute.  Does not store it in memory or the DB
     * @param muter The admin who muted them
     * @param time The time the mute was created
     */
	public Mute(String muter, long time){
		this.muter = muter;
                this.time = time;
	}
    
	/**
	 * Returns the admin who muted them's name as lowercase.
	 * @return The admin who muted them
	 */
    public String getMuter() {
        return muter;
    }
    
    /**
     * Returns the time that their mute was created
     * @return The time the mute was created
     */
    public long getTime(){
        return time;
    }
}