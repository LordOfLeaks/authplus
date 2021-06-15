package me.lordofleaks.authplus.spigot.listener;

import lombok.RequiredArgsConstructor;
import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;

@RequiredArgsConstructor
public class PreAuthListener implements Listener {

    private final AuthPlusCore core;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if(!isAuthorized(event.getPlayer())) {
            Block from = event.getFrom().getBlock();
            Block to = event.getTo().getBlock();
            if(!from.equals(to)) {
                //TODO set fall distance again
                event.setTo(event.getFrom().getBlock().getLocation().add(0.5, 0, 0.5));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!isAuthorized((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageOther(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (!isAuthorized((Player) event.getDamager())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageOther(PlayerInteractEntityEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageOther(PlayerInteractAtEntityEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(PlayerPickupItemEvent event) {
        if (!isAuthorized(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInvClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (!isAuthorized((Player) event.getWhoClicked()))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInvDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (!isAuthorized((Player) event.getWhoClicked()))
                event.setCancelled(true);
        }
    }

    private boolean isAuthorized(Player player) {
        Session session = core.getSessionStorage().getSessionByAccount(player.getUniqueId());
        return session != null && session.isAuthorized();
    }
}