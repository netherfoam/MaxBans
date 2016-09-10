package org.maxgamer.maxbans.database;

import java.sql.Connection;

public interface DatabaseCore
{
    Connection getConnection();
    
    void queue(BufferStatement p0);
    
    void flush();
    
    void close();
}
