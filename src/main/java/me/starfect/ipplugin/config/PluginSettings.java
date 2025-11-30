package me.starfect.ipplugin.config;

import org.bukkit.configuration.file.FileConfiguration;

public final class PluginSettings {
    public enum PermissionMode {
        OP,
        USER
    }

    private final String apiChoice;
    private final String apiToken;
    private final String helpCommand;
    private final String checkCommand;
    private final String allCommand;
    private final boolean countryBanEnabled;
    private final boolean userBanEnabled;
    private final PermissionMode permissionMode;

    private PluginSettings(FileConfiguration config) {
        this.apiChoice = read(config, "api.api", "a").toLowerCase();
        this.apiToken = read(config, "api.api-token", "");
        this.helpCommand = read(config, "custom.command-help", "help").toLowerCase();
        this.checkCommand = read(config, "custom.command-ip-check", "check").toLowerCase();
        this.allCommand = read(config, "custom.command-all", "all").toLowerCase();
        this.countryBanEnabled = config.getBoolean("country-ban", true);
        this.userBanEnabled = config.getBoolean("user-ban", true);
        this.permissionMode = "user".equalsIgnoreCase(read(config, "permission", "OP"))
                ? PermissionMode.USER
                : PermissionMode.OP;
    }

    public static PluginSettings from(FileConfiguration config) {
        return new PluginSettings(config);
    }

    public String apiChoice() {
        return apiChoice;
    }

    public String apiToken() {
        return apiToken;
    }

    public String helpCommand() {
        return helpCommand;
    }

    public String checkCommand() {
        return checkCommand;
    }

    public String allCommand() {
        return allCommand;
    }

    public boolean isCountryBanEnabled() {
        return countryBanEnabled;
    }

    public boolean isUserBanEnabled() {
        return userBanEnabled;
    }

    public PermissionMode permissionMode() {
        return permissionMode;
    }

    private String read(FileConfiguration config, String path, String def) {
        String value = config.getString(path, def);
        return value == null ? def : value.trim();
    }
}
