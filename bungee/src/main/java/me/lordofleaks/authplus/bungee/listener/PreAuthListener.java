package me.lordofleaks.authplus.bungee.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.session.Session;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@RequiredArgsConstructor
public class PreAuthListener implements Listener {

    private final AuthPlusCore core;

    @EventHandler
    public void onCommand(ChatEvent event) {
        if(event.getSender() instanceof ProxiedPlayer) {
            Session session = core.getSessionStorage().getSessionByAccount(((ProxiedPlayer) event.getSender()).getUniqueId());
            if ((session != null && session.isAuthorized())
                    || (event.getMessage().startsWith("/login") || event.getMessage().startsWith("/register"))) {
                return;
            }
            event.setCancelled(true);
        }
    }
}