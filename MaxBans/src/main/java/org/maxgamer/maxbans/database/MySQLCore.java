package org.maxgamer.maxbans.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

public class MySQLCore implements DatabaseCore
{
    private String url;
    private Properties info;
    //private static final int MAX_CONNECTIONS = 8;
    private static ArrayList<Connection> pool;
    
    static {
        MySQLCore.pool = new ArrayList<Connection>();
    }
    
    public MySQLCore(final String host, final String user, final String pass, final String database, final String port) {
        super();
        this.info = new Properties();
        this.info.put("autoReconnect", "true");
        this.info.put("user", user);
        this.info.put("password", pass);
        this.info.put("useUnicode", "true");
        this.info.put("characterEncoding", "utf8");
        this.url = ("jdbc:mysql://" + host + ":" + port + "/" + database);
        for (int i = 0; i < 8; i++) {
          pool.add(null);
        }
    }
    
    public Connection getConnection() {
        int i = 0;
        while (i < 8) {
            Connection connection = MySQLCore.pool.get(i);
            try {
                if (connection != null && !connection.isClosed() && connection.isValid(10)) {
                    return connection;
                }
                connection = DriverManager.getConnection(this.url, this.info);
                MySQLCore.pool.set(i, connection);
                return connection;
            }
            catch (SQLException e) {
                e.printStackTrace();
                ++i;
            }
        }
        return null;
    }
    
    public void queue(final BufferStatement bs) {
        try {
            final Connection con = this.getConnection();
            while (con == null) {
                try {
                    Thread.sleep(15L);
                }
                catch (InterruptedException ex) {}
                this.getConnection();
            }
            final PreparedStatement ps = bs.prepareStatement(con);
            ps.execute();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void close() {
    }
    
    public void flush() {
    }
}
