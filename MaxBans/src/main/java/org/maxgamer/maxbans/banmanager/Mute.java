package org.maxgamer.maxbans.banmanager;

public class Mute extends Punishment
{
    public Mute(final String muted, final String muter, final String reason, final long created) {
        super(muted, reason, muter, created);
    }
}
