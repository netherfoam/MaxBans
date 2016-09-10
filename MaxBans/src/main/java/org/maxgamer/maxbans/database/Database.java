package org.maxgamer.maxbans.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class Database
{
    private DatabaseCore core;
    
    public Database(final DatabaseCore core) throws ConnectionException {
        super();
        try {
            try {
                if (!core.getConnection().isValid(10)) {
                    throw new ConnectionException("Database doesn not appear to be valid!");
                }
            }
            catch (AbstractMethodError abstractMethodError) {}
        }
        catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
        this.core = core;
    }
    
    public DatabaseCore getCore() {
        return this.core;
    }
    
    public Connection getConnection() {
        return this.core.getConnection();
    }
    
    public void execute(final String query, final Object... objs) {
        final BufferStatement bs = new BufferStatement(query, objs);
        this.core.queue(bs);
    }
    
    public boolean hasTable(final String table) throws SQLException {
        final ResultSet rs = this.getConnection().getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                rs.close();
                return true;
            }
        }
        rs.close();
        return false;
    }
    
    public void close() {
        this.core.close();
    }
    
    public boolean hasColumn(final String table, final String column) throws SQLException {
        if (!this.hasTable(table)) {
            return false;
        }
        final String query = "SELECT * FROM " + table + " LIMIT 0,1";
        try {
            final PreparedStatement ps = this.getConnection().prepareStatement(query);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.getString(column);
                return true;
            }
        }
        catch (SQLException e) {
            return false;
        }
        return false;
    }
    
    public void copyTo(final Database db) throws SQLException {
        ResultSet rs = this.getConnection().getMetaData().getTables(null, null, "%", null);
        final List<String> tables = new LinkedList<String>();
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
        rs.close();
        this.core.flush();
        for (final String table : tables) {
            if (table.toLowerCase().startsWith("sqlite_autoindex_")) {
                continue;
            }
            System.out.println("Copying " + table);
            db.getConnection().prepareStatement("DELETE FROM " + table).execute();
            rs = this.getConnection().prepareStatement("SELECT * FROM " + table).executeQuery();
            int n = 0;
            String query = "INSERT INTO " + table + " VALUES (";
            query = String.valueOf(query) + "?";
            for (int i = 2; i <= rs.getMetaData().getColumnCount(); ++i) {
                query = String.valueOf(query) + ", ?";
            }
            query = String.valueOf(query) + ")";
            final PreparedStatement ps = db.getConnection().prepareStatement(query);
            while (rs.next()) {
                ++n;
                for (int j = 1; j <= rs.getMetaData().getColumnCount(); ++j) {
                    ps.setObject(j, rs.getObject(j));
                }
                ps.addBatch();
                if (n % 100 == 0) {
                    ps.executeBatch();
                    System.out.println(String.valueOf(n) + " records copied...");
                }
            }
            ps.executeBatch();
            rs.close();
        }
        db.getConnection().close();
        this.getConnection().close();
    }
    
    public static class ConnectionException extends Exception
    {
        private static final long serialVersionUID = 8348749992936357317L;
        
        public ConnectionException(final String msg) {
            super(msg);
        }
    }
}
