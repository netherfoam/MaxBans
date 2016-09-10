package org.maxgamer.maxbans.commands.bridge;

import java.sql.SQLException;
import org.maxgamer.maxbans.MaxBans;
import org.maxgamer.maxbans.database.Database;

public class SQLiteBridge implements Bridge
{
    private Database db;
    
    public SQLiteBridge(final Database db) {
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
