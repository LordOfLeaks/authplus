package me.lordofleaks.authplus.spigot.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.comm.Communicator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

@RequiredArgsConstructor
public class AuthPlusPluginMessageListener implements PluginMessageListener {

    private final Communicator communicator;

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if(channel.equals(Communicator.CHANNEL_NAME)) {
            communicator.handleRead(player.getUniqueId(), data).exceptionally(ex -> {
                ex.printStackTrace();
                return null;
            });
        }
    }
}