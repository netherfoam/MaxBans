package org.maxgamer.maxbans.banmanager;

public class TempBan extends Ban{
        private long timeOfUnban;
        
	public TempBan(String reason, String banner, long time, long unbanTime){
            super(reason, banner, time);
            timeOfUnban = unbanTime;
	}
        
        public long getTimeOfUnban() {
            return timeOfUnban;
        }
}