package me.starfect.ipplugin;

import me.starfect.ipplugin.api.ApiClient;
import me.starfect.ipplugin.api.IpApiClient;
import me.starfect.ipplugin.api.IpInfoClient;
import me.starfect.ipplugin.command.IpCommand;
import me.starfect.ipplugin.config.PluginSettings;
import me.starfect.ipplugin.data.BanStorage;
import me.starfect.ipplugin.data.UserListStorage;
import me.starfect.ipplugin.listener.JoinListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class IPPlugin extends JavaPlugin {
    private PluginSettings settings;
    private ApiClient apiClient;
    private BanStorage banStorage;
    private UserListStorage userListStorage;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("banlist.yml");
        saveResourceIfMissing("userlist.yml");

        this.settings = PluginSettings.from(getConfig());
        this.apiClient = createApiClient();
        this.banStorage = new BanStorage(this);
        this.userListStorage = new UserListStorage(this);

        IpCommand ipCommand = new IpCommand(this, settings, apiClient, banStorage, userListStorage);
        PluginCommand command = getCommand("ip");
        if (command != null) {
            command.setExecutor(ipCommand);
            command.setTabCompleter(ipCommand);
        } else {
            getLogger().severe("Command /ip not found in plugin.yml");
        }

        getServer().getPluginManager().registerEvents(
                new JoinListener(this, apiClient, settings, banStorage, userListStorage), this);

        getLogger().info("IPPlugin enabled using provider: " + apiClient.getName());
    }

    @Override
    public void onDisable() {
        if (banStorage != null) {
            banStorage.save();
        }
        if (userListStorage != null) {
            userListStorage.save();
        }
    }

    private ApiClient createApiClient() {
        if ("b".equalsIgnoreCase(settings.apiChoice())) {
            return new IpInfoClient(settings.apiToken());
        }
        return new IpApiClient(settings.apiToken());
    }

    private void saveResourceIfMissing(String name) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (!new java.io.File(getDataFolder(), name).exists()) {
            saveResource(name, false);
        }
    }
}
