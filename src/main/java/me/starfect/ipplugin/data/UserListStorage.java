package me.starfect.ipplugin.data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public final class UserListStorage {
    private final Plugin plugin;
    private final File file;
    private YamlConfiguration yaml;
    private Set<String> visitedUsers;

    public UserListStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "userlist.yml");
        load();
    }

    public void load() {
        if (!file.exists()) {
            plugin.saveResource("userlist.yml", false);
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        this.visitedUsers = new HashSet<>(yaml.getStringList("user-list"));
    }

    public synchronized void save() {
        yaml.set("user-list", visitedUsers.stream().toList());
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save userlist.yml", e);
        }
    }

    public synchronized boolean addUser(String username) {
        boolean added = visitedUsers.add(username.toLowerCase(Locale.ROOT));
        if (added) {
            save();
        }
        return added;
    }

    public synchronized Set<String> getUsers() {
        return new HashSet<>(visitedUsers);
    }
}
