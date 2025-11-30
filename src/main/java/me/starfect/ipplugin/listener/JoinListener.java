package me.starfect.ipplugin.listener;

import java.net.InetSocketAddress;
import me.starfect.ipplugin.IPPlugin;
import me.starfect.ipplugin.api.ApiClient;
import me.starfect.ipplugin.config.PluginSettings;
import me.starfect.ipplugin.data.BanStorage;
import me.starfect.ipplugin.data.IpInfo;
import me.starfect.ipplugin.data.UserListStorage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {
    private final IPPlugin plugin;
    private final ApiClient apiClient;
    private final PluginSettings settings;
    private final BanStorage banStorage;
    private final UserListStorage userListStorage;

    public JoinListener(IPPlugin plugin, ApiClient apiClient, PluginSettings settings, BanStorage banStorage,
                        UserListStorage userListStorage) {
        this.plugin = plugin;
        this.apiClient = apiClient;
        this.settings = settings;
        this.banStorage = banStorage;
        this.userListStorage = userListStorage;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        userListStorage.addUser(player.getName());

        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return;
        }
        String ip = address.getAddress().getHostAddress();

        if (settings.isUserBanEnabled() && banStorage.isUserBanned(player.getName())) {
            kick(player, ChatColor.RED + "접속이 차단되었습니다. (유저 밴)");
            return;
        }
        if (settings.isUserBanEnabled() && banStorage.isUserBanned(ip)) {
            kick(player, ChatColor.RED + "접속이 차단되었습니다. (IP 밴)");
            return;
        }

        if (settings.isCountryBanEnabled()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    apiClient.lookup(ip).thenAccept(info -> handleCountryCheck(player, info))
                            .exceptionally(error -> {
                                plugin.getLogger().warning("IP lookup 실패: " + error.getMessage());
                                return null;
                            }));
        }
    }

    private void handleCountryCheck(Player player, IpInfo info) {
        if (info == null) {
            return;
        }
        String code = info.countryCode();
        if (code == null) {
            return;
        }
        if (banStorage.isCountryBanned(code)) {
            kick(player, ChatColor.RED + "접속이 차단되었습니다. (국가 밴: " + code + ")");
        }
    }

    private void kick(Player player, String reason) {
        Bukkit.getScheduler().runTask(plugin, () ->
                player.kick(Component.text(ChatColor.translateAlternateColorCodes('&', reason))));
    }
}
