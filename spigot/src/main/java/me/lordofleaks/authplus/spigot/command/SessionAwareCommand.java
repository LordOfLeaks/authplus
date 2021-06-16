package me.lordofleaks.authplus.spigot.command;

import me.lordofleaks.authplus.core.AuthPlusCore;
import me.lordofleaks.authplus.core.session.Session;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class SessionAwareCommand implements CommandExecutor {

    private final AuthPlusCore core;

    public SessionAwareCommand(AuthPlusCore core) {
        this.core = core;
        this.core.getMessageConfiguration().registerMessage("command-session-load-in-progress",
                "&7Hang on! We are fetching your session...");
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be executed by a player");
            return false;
        }
        Player player = (Player) sender;
        Session session = core.getSessionStorage().getSessionByAccount(player.getUniqueId());
        if (session == null) {
            sender.sendMessage(core.getMessageConfiguration().getMessage("command-login-session-load-in-progress"));
            return true;
        }
        return onCommand(player, session, label, args);
    }

    protected abstract boolean onCommand(Player sender, Session session, String label, String[] args);
}