package org.maxgamer.maxbans.banmanager;

import org.maxgamer.maxbans.Msg;
import org.maxgamer.maxbans.MaxBans;

public class Ban extends Punishment
{
    public Ban(final String user, final String reason, final String banner, final long created) {
        super(user, reason, banner, created);
    }
    
    public String getKickMessage() {
        return Msg.get("disconnection.you-are-banned", new String[] { "reason", "banner", "appeal-message" }, new String[] { this.getReason(), this.getBanner(), MaxBans.instance.getBanManager().getAppealMessage() });
    }
}
