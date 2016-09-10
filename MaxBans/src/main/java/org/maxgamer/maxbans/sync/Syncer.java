package org.maxgamer.maxbans.sync;

public class Syncer
{
    private ClientToServerConnection con;
    
    public Syncer(final String host, final int port, String pass) {
        super();
        try {
            pass = SyncUtil.encrypt(pass, "fuQJ7_q#eF78A&D");
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to start Syncer: " + e.getMessage());
        }
        this.con = new ClientToServerConnection(host, port, pass);
    }
    
    public void start() {
        this.con.start();
    }
    
    public void stop() {
        this.con.close();
    }
    
    protected void write(final Packet p) {
        this.con.write(p);
    }
    
    public void broadcast(final Packet p) {
        p.put("broadcast", null);
        this.write(p);
    }
}
