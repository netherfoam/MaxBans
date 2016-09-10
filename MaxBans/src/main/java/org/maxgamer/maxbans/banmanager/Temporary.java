package org.maxgamer.maxbans.banmanager;

public interface Temporary
{
    long getExpires();
    
    boolean hasExpired();
}
