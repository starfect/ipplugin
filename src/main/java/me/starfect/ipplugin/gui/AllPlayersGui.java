package me.starfect.ipplugin.gui;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.starfect.ipplugin.IPPlugin;
import me.starfect.ipplugin.config.PluginSettings;
import me.starfect.ipplugin.data.BanStorage;
import me.starfect.ipplugin.util.PermissionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class AllPlayersGui implements Listener {
    private final IPPlugin plugin;
    private final BanStorage banStorage;
    private final PluginSettings settings;
    private final Map<Inventory, Map<Integer, UUID>> inventoryIndex;

    public AllPlayersGui(IPPlugin plugin, PluginSettings settings, BanStorage banStorage) {
        this.plugin = plugin;
        this.settings = settings;
        this.banStorage = banStorage;
        this.inventoryIndex = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player viewer) {
        int online = Bukkit.getOnlinePlayers().size();
        int size = Math.min(Math.max(9, ((online + 8) / 9) * 9), 54);
        Inventory inventory = Bukkit.createInventory(viewer, size, ChatColor.DARK_AQUA + "IP ALL");

        Map<Integer, UUID> slotMap = new HashMap<>();
        int slot = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (slot >= size) {
                break;
            }
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(target);
                meta.setDisplayName(ChatColor.YELLOW + target.getName());
                String ip = getIp(target);
                boolean banned = (ip != null && banStorage.isUserBanned(ip)) || banStorage.isUserBanned(target.getName());
                meta.setLore(List.of(
                        ChatColor.GRAY + "IP: " + (ip == null ? "알 수 없음" : ip),
                        banned ? ChatColor.RED + "밴됨 (좌클릭으로 해제)" : ChatColor.GREEN + "정상 (좌클릭으로 밴)",
                        ChatColor.AQUA + "우클릭: 강제 퇴장"
                ));
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                skull.setItemMeta(meta);
            }
            inventory.setItem(slot, skull);
            slotMap.put(slot, target.getUniqueId());
            slot++;
        }

        inventoryIndex.put(inventory, slotMap);
        viewer.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory inventory = event.getInventory();
        Map<Integer, UUID> slotMap = inventoryIndex.get(inventory);
        if (slotMap == null) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getRawSlot();
        if (!slotMap.containsKey(slot)) {
            return;
        }
        UUID targetId = slotMap.get(slot);
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "대상 플레이어가 없습니다.");
            return;
        }
        if (!PermissionUtil.hasAdminAccess(player, settings)) {
            player.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return;
        }
        String ip = getIp(target);
        if (event.isLeftClick()) {
            if (ip == null) {
                player.sendMessage(ChatColor.RED + "IP 정보를 가져올 수 없습니다.");
                return;
            }
            if (banStorage.isUserBanned(ip)) {
                banStorage.pardonUser(ip);
                banStorage.pardonUser(target.getName());
                player.sendMessage(ChatColor.GREEN + target.getName() + " 의 IP 밴을 해제했습니다.");
            } else {
                banStorage.banUser(ip);
                banStorage.banUser(target.getName());
                player.sendMessage(ChatColor.RED + target.getName() + " 의 IP를 밴했습니다.");
                target.kick(Component.text(ChatColor.RED + "IP가 밴되어 퇴장됩니다."));
            }
            open(player);
        } else if (event.isRightClick()) {
            target.kick(Component.text(ChatColor.RED + "관리자에 의해 퇴장되었습니다."));
            player.sendMessage(ChatColor.YELLOW + target.getName() + " 을(를) 퇴장시켰습니다.");
        }
    }

    private String getIp(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }
}
