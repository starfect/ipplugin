package me.starfect.ipplugin.util;

import me.starfect.ipplugin.config.PluginSettings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class PermissionUtil {
    private PermissionUtil() {
    }

    public static boolean hasUserAccess(CommandSender sender, PluginSettings settings) {
        if (settings.permissionMode() == PluginSettings.PermissionMode.OP) {
            return sender.isOp();
        }
        return sender.hasPermission("ipplugin.user") || sender.isOp();
    }

    public static boolean hasAdminAccess(CommandSender sender, PluginSettings settings) {
        if (settings.permissionMode() == PluginSettings.PermissionMode.OP) {
            return sender.isOp();
        }
        return sender.hasPermission("ipplugin.admin") || sender.isOp();
    }

    public static boolean ensurePlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
