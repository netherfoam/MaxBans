package org.maxgamer.maxbans.commands.bridge;

import java.sql.SQLException;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;

public class MySQLBridge implements Bridge
{
    private Database db;
    
    public MySQLBridge(final Database db) {
        super();
        this.db = db;
    }
    
    public void export() throws SQLException {
        MaxBans.instance.getDB().copyTo(this.db);
    }
    
    public void load() throws SQLException {
        this.db.copyTo(MaxBans.instance.getDB());
    }
}
