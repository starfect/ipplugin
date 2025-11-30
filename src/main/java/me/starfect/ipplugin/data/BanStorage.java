package me.starfect.ipplugin.data;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class BanStorage {
    private final Plugin plugin;
    private final File file;
    private YamlConfiguration yaml;
    private Set<String> bannedUsers;
    private Set<String> bannedCountries;

    public BanStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "banlist.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("banlist.yml", false);
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.bannedUsers = new HashSet<>(yaml.getConfigurationSection("ban-user") == null
                ? Collections.emptySet()
                : yaml.getConfigurationSection("ban-user").getKeys(false));
        this.bannedCountries = new HashSet<>(yaml.getConfigurationSection("ban-country") == null
                ? Collections.emptySet()
                : yaml.getConfigurationSection("ban-country").getKeys(false));
    }

    public synchronized void save() {
        yaml.set("ban-user", null);
        yaml.set("ban-country", null);
        for (String name : bannedUsers) {
            yaml.set("ban-user." + name, "ban");
        }
        for (String code : bannedCountries) {
            yaml.set("ban-country." + code, "ban");
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save banlist.yml", e);
        }
    }

    public synchronized boolean isUserBanned(String nameOrIp) {
        String key = normalize(nameOrIp);
        return bannedUsers.stream().anyMatch(entry -> entry.equalsIgnoreCase(key));
    }

    public synchronized boolean banUser(String nameOrIp) {
        boolean added = bannedUsers.add(normalize(nameOrIp));
        save();
        return added;
    }

    public synchronized boolean pardonUser(String nameOrIp) {
        boolean removed = bannedUsers.remove(normalize(nameOrIp));
        save();
        return removed;
    }

    public synchronized boolean isCountryBanned(String countryCode) {
        String key = countryCode.toUpperCase(Locale.ROOT);
        return bannedCountries.contains(key);
    }

    public synchronized boolean banCountry(String countryCode) {
        boolean added = bannedCountries.add(countryCode.toUpperCase(Locale.ROOT));
        save();
        return added;
    }

    public synchronized boolean pardonCountry(String countryCode) {
        boolean removed = bannedCountries.remove(countryCode.toUpperCase(Locale.ROOT));
        save();
        return removed;
    }

    public synchronized Set<String> getBannedUsers() {
        return new HashSet<>(bannedUsers);
    }

    public synchronized Set<String> getBannedCountries() {
        return new HashSet<>(bannedCountries);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
