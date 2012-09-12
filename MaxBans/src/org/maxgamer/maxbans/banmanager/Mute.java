package org.maxgamer.maxbans.banmanager;

public class Mute{
        private String muter;
        private long timeOfMute;
        
	public Mute(String muter, long time){
		this.muter = muter;
                this.timeOfMute = time;
	}
        
        public String getMuter() {
            return muter;
        }
        
        public long getTimeOfMute(){
            return timeOfMute;
        }
}