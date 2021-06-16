package me.lordofleaks.authplus.bungee.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.comm.Communicator;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@RequiredArgsConstructor
public class AuthPlusPluginMessageListener implements Listener {

    private final AuthPlusCore core;

    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if(event.getTag().equals(Communicator.CHANNEL_NAME)) {
           if(event.getReceiver() instanceof ProxiedPlayer) {
                core.getCommunicator().handleRead(((ProxiedPlayer) event.getReceiver()).getUniqueId(), event.getData().clone())
                        .exceptionally(ex -> {
                            ex.printStackTrace();
                            return null;
                        });
            }
        }
    }

}