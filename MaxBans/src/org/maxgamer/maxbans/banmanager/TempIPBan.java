package org.maxgamer.maxbans.banmanager;

public class TempIPBan extends IPBan{
    private long timeOfUnban;
    
    public TempIPBan(String reason, String banner, long timeOfBan, String bannedIP, long unbanTime){
        super (reason, banner, timeOfBan, bannedIP);
        timeOfUnban=unbanTime;
    }
    
    public long getTimeOfUnban() {
        return timeOfUnban;
    }
}