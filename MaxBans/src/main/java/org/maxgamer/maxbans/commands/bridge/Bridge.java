package org.maxgamer.maxbans.commands.bridge;

public interface Bridge
{
    void export() throws Exception;
    
    void load() throws Exception;
}
