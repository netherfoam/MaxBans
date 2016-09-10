package org.maxgamer.maxbans.database;

import java.sql.PreparedStatement;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.io.File;
import java.sql.Connection;

public class SQLiteCore implements DatabaseCore
{
    private Connection connection;
    private File dbFile;
    private Thread watcher;
    private volatile LinkedList<BufferStatement> queue;
    
    public SQLiteCore(final File dbFile) {
        super();
        this.queue = new LinkedList<BufferStatement>();
        this.dbFile = dbFile;
    }
    
    public Connection getConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                return this.connection;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        if (this.dbFile.exists()) {
            try {
                Class.forName("org.sqlite.JDBC");
                return this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
            }
            catch (ClassNotFoundException e2) {
                e2.printStackTrace();
                return null;
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }
        try {
            this.dbFile.createNewFile();
            return this.getConnection();
        }
        catch (IOException e3) {
            e3.printStackTrace();
            return null;
        }
    }
    
    public void queue(final BufferStatement bs) {
        synchronized (this.queue) {
            this.queue.add(bs);
        }
        // monitorexit(this.queue)
        if (this.watcher == null || !this.watcher.isAlive()) {
            this.startWatcher();
        }
    }
    
    public void flush() {
        while (!this.queue.isEmpty()) {
            final BufferStatement bs;
            synchronized (this.queue) {
                bs = this.queue.removeFirst();
            }
            // monitorexit(this.queue)
            try {
                final PreparedStatement ps = bs.prepareStatement(this.getConnection());
                ps.execute();
                ps.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void close() {
        this.flush();
    }
    
    private void startWatcher() {
        (this.watcher = new Thread() {
            public void run() {
                try {
                    Thread.sleep(30000L);
                }
                catch (InterruptedException ex) {}
                SQLiteCore.this.flush();
            }
        }).start();
    }
}
