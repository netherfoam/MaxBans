package org.maxgamer.maxbans.banmanager;

public class IPBan extends Ban{
        private String ip;
        
	public IPBan(String reason, String banner, long time, String newIP){
            super(reason,banner,time);
            ip=newIP;
	}
        
        public String getIp(){
            return ip;
        }
}