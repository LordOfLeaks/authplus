package me.lordofleaks.authplus.bungee.listener;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.session.Session;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class LoginListener implements Listener {

    private final Map<PendingConnection, Session> pendingSessions = new WeakHashMap<>();
    private final Plugin plugin;
    private final AuthPlusCore core;

    public LoginListener(Plugin plugin, AuthPlusCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (!core.getAccountValidator().isAccountNameValid(event.getConnection().getName())) {
            event.setCancelled(true);
            event.setCancelReason("Invalid name");
            return;
        }

        event.registerIntent(plugin);
        core.getLoginEngine().performLogin(event.getConnection().getName(), (name) ->
                UUID.nameUUIDFromBytes((name + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8))
        ).thenAccept(session -> {
            event.getConnection().setOnlineMode(session.getAccount().isRegisteredPremium());
            if (session.getAccount().isRegisteredPremium()) {
                session.setAuthorized(true);
            } else {
                event.getConnection().setUniqueId(session.getAccount().getUniqueId());
            }
            pendingSessions.put(event.getConnection(), session);
            event.completeIntent(plugin);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            event.setCancelReason("Exception in fetching from account repository");
            event.setCancelled(true);
            event.completeIntent(plugin);
            return null;
        });
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        Session session = pendingSessions.remove(event.getPlayer().getPendingConnection());
        if (session == null) {
            event.getPlayer().disconnect(new TextComponent("You somehow skipped PreLogin :O"));
            return;
        }
        core.getSessionStorage().insertSession(session);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        pendingSessions.remove(event.getPlayer().getPendingConnection());
        core.getSessionStorage().deleteSession(event.getPlayer().getUniqueId());
    }
}