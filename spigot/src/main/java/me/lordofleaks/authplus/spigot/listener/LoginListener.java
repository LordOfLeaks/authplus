package me.lordofleaks.authplus.spigot.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.config.msg.MessageArg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class LoginListener implements Listener {

    private final JavaPlugin plugin;
    private final AuthPlusCore core;
    private final boolean bungeeDisabled;

    public LoginListener(JavaPlugin plugin, AuthPlusCore core, boolean bungeeDisabled) {
        this.plugin = plugin;
        this.core = core;
        this.bungeeDisabled = bungeeDisabled;
        this.core.getMessageConfiguration().registerMessage("login-invalid-account-name",
                "&cAccount name \"%accountName%\" is invalid.");
        this.core.getMessageConfiguration().registerMessage("login-perform-login-failed",
                "&cLogin failed due to database error.\n&cPlease try again in a minute.");
        this.core.getMessageConfiguration().registerMessage("login-retrieve-session-failed",
                "&cCannot retrieve your session.\n&cPlease try again in a minute.");
        this.core.getMessageConfiguration().registerMessage("login-direct-connection",
                "&cPlease join via proxy.");
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if(!core.getAccountValidator().isAccountNameValid(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    core.getMessageConfiguration().getMessage("login-invalid-account-name",
                            MessageArg.of("accountName", event.getName())
                    )
            );
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (bungeeDisabled) {
            if(core.getLoginEngine() == null) {
                disconnectSynchronously(event.getPlayer(), "Login engine not initialized.");
                return;
            }
            final UUID uuid = event.getPlayer().getUniqueId();
            core.getLoginEngine().performLogin(event.getPlayer().getName(), (name) -> uuid)
                    .thenAccept(session -> Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getPlayer().isOnline()) {
                            core.getSessionStorage().insertSession(session);
                        }
                    })).exceptionally(e -> {
                        e.printStackTrace();
                        disconnectSynchronously(event.getPlayer(), core.getMessageConfiguration().getMessage("login-perform-login-failed"));
                        return null;
                    });
        } else {
            core.getCommunicator().getSession(event.getPlayer().getUniqueId()).thenAccept(session ->
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (event.getPlayer().isOnline()) {
                            if (session == null) {
                                event.getPlayer().kickPlayer(core.getMessageConfiguration().getMessage("login-direct-connection"));
                            } else {
                                core.getSessionStorage().insertSession(session);
                            }
                        }
                    })
            ).exceptionally(e -> {
                e.printStackTrace();
                disconnectSynchronously(event.getPlayer(), core.getMessageConfiguration().getMessage("login-retrieve-session-failed"));
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