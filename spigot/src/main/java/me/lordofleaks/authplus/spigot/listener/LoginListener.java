package me.lordofleaks.authplus.spigot.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@RequiredArgsConstructor
public class LoginListener implements Listener {

    private final JavaPlugin plugin;
    private final AuthPlusCore core;
    private final boolean bungeeDisabled;

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (bungeeDisabled) {
            final UUID uuid = event.getPlayer().getUniqueId();
            core.getLoginEngine().performLogin(event.getPlayer().getName(), (name) -> uuid)
                    .thenAccept(session -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getPlayer().isOnline()) {
                            core.getSessionStorage().insertSession(session);
                        }
                    })).exceptionally(e -> {
                        e.printStackTrace();
                        disconnectSynchronously(event.getPlayer(), "Failed to log in");
                        return null;
                    });
        } else {
            core.getCommunicator().getSession(event.getPlayer().getUniqueId()).thenAccept(session ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getPlayer().isOnline()) {
                            if (session == null) {
                                event.getPlayer().kickPlayer("Please join via proxy");
                            } else {
                                core.getSessionStorage().insertSession(session);
                            }
                        }
                    })
            ).exceptionally(e -> {
                e.printStackTrace();
                disconnectSynchronously(event.getPlayer(), "Failed to retrieve session");
                return null;
            });
        }
    }

    private void disconnectSynchronously(Player player, String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.kickPlayer(reason);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        core.getSessionStorage().deleteSession(event.getPlayer().getUniqueId());
    }
}