package org.maxgamer.maxbans.banmanager;

public class TempMute extends Mute{
        private long timeOfUnmute;
        
	public TempMute(String muter, long time, long liftMute){
		super (muter, time);
                timeOfUnmute = liftMute;
	}
        
        public long getTimeOfUnmute() {
            return timeOfUnmute;
        }
}