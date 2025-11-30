package me.starfect.ipplugin.command;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import me.starfect.ipplugin.IPPlugin;
import me.starfect.ipplugin.api.ApiClient;
import me.starfect.ipplugin.config.PluginSettings;
import me.starfect.ipplugin.data.BanStorage;
import me.starfect.ipplugin.data.IpInfo;
import me.starfect.ipplugin.data.UserListStorage;
import me.starfect.ipplugin.gui.AllPlayersGui;
import me.starfect.ipplugin.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class IpCommand implements CommandExecutor, TabCompleter {
    private final IPPlugin plugin;
    private final PluginSettings settings;
    private final ApiClient apiClient;
    private final BanStorage banStorage;
    private final UserListStorage userListStorage;
    private final AllPlayersGui allPlayersGui;

    public IpCommand(IPPlugin plugin, PluginSettings settings, ApiClient apiClient, BanStorage banStorage,
                     UserListStorage userListStorage) {
        this.plugin = plugin;
        this.settings = settings;
        this.apiClient = apiClient;
        this.banStorage = banStorage;
        this.userListStorage = userListStorage;
        this.allPlayersGui = new AllPlayersGui(plugin, settings, banStorage);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals(settings.helpCommand())) {
            sendHelp(sender);
            return true;
        }
        if (sub.equals(settings.checkCommand())) {
            handleCheck(sender, args);
            return true;
        }
        if (sub.equalsIgnoreCase("ban")) {
            handleBan(sender, args);
            return true;
        }
        if (sub.equalsIgnoreCase("pardon")) {
            handlePardon(sender, args);
            return true;
        }
        if (sub.equalsIgnoreCase("status")) {
            handleStatus(sender);
            return true;
        }
        if (sub.equalsIgnoreCase("banlist")) {
            handleBanList(sender);
            return true;
        }
        if (sub.equals(settings.allCommand())) {
            handleAll(sender);
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void handleCheck(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasUserAccess(sender, settings)) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return;
        }
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "콘솔에서는 대상을 지정하세요. /ip check <username>");
                return;
            }
            Player player = (Player) sender;
            String ip = getPlayerIp(player);
            if (ip == null) {
                sender.sendMessage(ChatColor.RED + "IP 정보를 가져올 수 없습니다.");
                return;
            }
            lookupAndSend(sender, player.getName(), ip);
            return;
        }
        if (!PermissionUtil.hasAdminAccess(sender, settings)) {
            sender.sendMessage(ChatColor.RED + "이 기능은 관리자만 사용할 수 있습니다.");
            return;
        }
        String targetName = args[1];
        Player target = Bukkit.getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "온라인 플레이어가 아닙니다.");
            return;
        }
        String ip = getPlayerIp(target);
        if (ip == null) {
            sender.sendMessage(ChatColor.RED + "IP 정보를 가져올 수 없습니다.");
            return;
        }
        lookupAndSend(sender, target.getName(), ip);
    }

    private void lookupAndSend(CommandSender sender, String targetName, String ip) {
        sender.sendMessage(ChatColor.YELLOW + "IP 조회 중... (" + targetName + ")");
        CompletableFuture<IpInfo> future = apiClient.lookup(ip);
        future.thenAccept(info -> Bukkit.getScheduler().runTask(plugin, () -> sendInfo(sender, targetName, info)))
                .exceptionally(error -> {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            sender.sendMessage(ChatColor.RED + "조회 실패: " + error.getMessage()));
                    return null;
                });
    }

    private void sendInfo(CommandSender sender, String targetName, IpInfo info) {
        sender.sendMessage(ChatColor.AQUA + "=== IP 정보: " + targetName + " ===");
        sender.sendMessage(ChatColor.GRAY + "IP: " + ChatColor.WHITE + info.ip());
        sender.sendMessage(ChatColor.GRAY + "국가: " + ChatColor.WHITE + info.country() + " (" + info.countryCode() + ")");
        sender.sendMessage(ChatColor.GRAY + "지역: " + ChatColor.WHITE + info.region());
        sender.sendMessage(ChatColor.GRAY + "도시: " + ChatColor.WHITE + info.city());
        sender.sendMessage(ChatColor.GRAY + "ISP: " + ChatColor.WHITE + info.isp());
        sender.sendMessage(ChatColor.GRAY + "ORG: " + ChatColor.WHITE + info.org());
        sender.sendMessage(ChatColor.GRAY + "시간대: " + ChatColor.WHITE + info.timezone());
        sender.sendMessage(ChatColor.GRAY + "위치: " + ChatColor.WHITE +
                info.latitude().map(String::valueOf).orElse("?") + ", " +
                info.longitude().map(String::valueOf).orElse("?"));
        sender.sendMessage(ChatColor.GRAY + "제공자: " + ChatColor.WHITE + info.provider());
    }

    private void handleBan(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasAdminAccess(sender, settings)) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법: /ip ban <username|ip|CountryCode>");
            return;
        }
        String target = args[1];
        if (isCountryCode(target)) {
            boolean added = banStorage.banCountry(target);
            sender.sendMessage(added
                    ? ChatColor.RED + "국가 " + target.toUpperCase(Locale.ROOT) + " 를 차단했습니다."
                    : ChatColor.YELLOW + "이미 차단된 국가입니다.");
            return;
        }

        Player player = Bukkit.getPlayerExact(target);
        if (player != null) {
            String ip = getPlayerIp(player);
            if (ip == null || ip.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "IP를 확인할 수 없습니다.");
                return;
            }
            boolean added = banStorage.banUser(ip);
            banStorage.banUser(player.getName());
            player.kick(ChatColor.RED + "IP 밴 처리되었습니다.");
            sender.sendMessage(added
                    ? ChatColor.RED + "IP " + ip + " 를 밴했습니다."
                    : ChatColor.YELLOW + "이미 밴된 IP/유저입니다.");
            return;
        }

        if (isIp(target)) {
            boolean added = banStorage.banUser(target);
            sender.sendMessage(added
                    ? ChatColor.RED + "IP " + target + " 를 밴했습니다."
                    : ChatColor.YELLOW + "이미 밴된 IP입니다.");
            return;
        }

        boolean added = banStorage.banUser(target);
        sender.sendMessage(added
                ? ChatColor.RED + "유저 " + target + " 를 밴 목록에 추가했습니다."
                : ChatColor.YELLOW + "이미 밴된 유저입니다.");
    }

    private void handlePardon(CommandSender sender, String[] args) {
        if (!PermissionUtil.hasAdminAccess(sender, settings)) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "사용법: /ip pardon <username|ip|CountryCode>");
            return;
        }
        String target = args[1];
        if (isCountryCode(target)) {
            boolean removed = banStorage.pardonCountry(target);
            sender.sendMessage(removed
                    ? ChatColor.GREEN + "국가 " + target.toUpperCase(Locale.ROOT) + " 차단을 해제했습니다."
                    : ChatColor.YELLOW + "해당 국가가 차단 목록에 없습니다.");
            return;
        }

        Player player = Bukkit.getPlayerExact(target);
        if (player != null) {
            String ip = getPlayerIp(player);
            if (ip != null) {
                banStorage.pardonUser(ip);
            }
            banStorage.pardonUser(player.getName());
            sender.sendMessage(ChatColor.GREEN + player.getName() + " 관련 밴을 해제했습니다.");
            return;
        }

        if (isIp(target)) {
            boolean removed = banStorage.pardonUser(target);
            sender.sendMessage(removed
                    ? ChatColor.GREEN + "IP " + target + " 밴을 해제했습니다."
                    : ChatColor.YELLOW + "해당 IP는 밴 목록에 없습니다.");
            return;
        }

        boolean removed = banStorage.pardonUser(target);
        sender.sendMessage(removed
                ? ChatColor.GREEN + "유저 " + target + " 밴을 해제했습니다."
                : ChatColor.YELLOW + "해당 유저는 밴 목록에 없습니다.");
    }

    private void handleStatus(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== IPPlugin 상태 ===");
        sender.sendMessage(ChatColor.GRAY + "API: " + ChatColor.WHITE + apiClient.getName());
        sender.sendMessage(ChatColor.GRAY + "API 토큰 설정: " + ChatColor.WHITE +
                (settings.apiToken().isEmpty() ? "없음" : "설정됨"));
        sender.sendMessage(ChatColor.GRAY + "국가 밴: " + ChatColor.WHITE + (settings.isCountryBanEnabled() ? "사용" : "비활성"));
        sender.sendMessage(ChatColor.GRAY + "유저/IP 밴: " + ChatColor.WHITE + (settings.isUserBanEnabled() ? "사용" : "비활성"));
        sender.sendMessage(ChatColor.GRAY + "권한 모드: " + ChatColor.WHITE + settings.permissionMode().name());
        sender.sendMessage(ChatColor.GRAY + "밴된 IP 수: " + ChatColor.WHITE + banStorage.getBannedUsers().size());
        sender.sendMessage(ChatColor.GRAY + "밴된 국가 수: " + ChatColor.WHITE + banStorage.getBannedCountries().size());
    }

    private void handleBanList(CommandSender sender) {
        Set<String> users = banStorage.getBannedUsers();
        Set<String> countries = banStorage.getBannedCountries();
        sender.sendMessage(ChatColor.AQUA + "=== 밴 목록 ===");
        sender.sendMessage(ChatColor.GRAY + "IP/유저: " + ChatColor.WHITE + (users.isEmpty() ? "없음" : String.join(", ", users)));
        sender.sendMessage(ChatColor.GRAY + "국가: " + ChatColor.WHITE + (countries.isEmpty() ? "없음" : String.join(", ", countries)));
    }

    private void handleAll(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "플레이어만 사용 가능합니다.");
            return;
        }
        if (!PermissionUtil.hasAdminAccess(sender, settings)) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return;
        }
        allPlayersGui.open(player);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "[IPPlugin] 사용 가능한 명령어:");
        sender.sendMessage(ChatColor.GRAY + "/ip " + settings.helpCommand() + ChatColor.WHITE + " - 도움말");
        sender.sendMessage(ChatColor.GRAY + "/ip " + settings.checkCommand() + ChatColor.WHITE + " - 자신의 IP 확인");
        sender.sendMessage(ChatColor.GRAY + "/ip " + settings.checkCommand() + " <username>" + ChatColor.WHITE + " - 대상 IP 확인 (관리자)");
        sender.sendMessage(ChatColor.GRAY + "/ip ban <username|ip|CountryCode>" + ChatColor.WHITE + " - IP/국가 밴 (관리자)");
        sender.sendMessage(ChatColor.GRAY + "/ip pardon <username|ip|CountryCode>" + ChatColor.WHITE + " - 밴 해제 (관리자)");
        sender.sendMessage(ChatColor.GRAY + "/ip status" + ChatColor.WHITE + " - 상태 확인");
        sender.sendMessage(ChatColor.GRAY + "/ip banlist" + ChatColor.WHITE + " - 밴 목록 보기");
        sender.sendMessage(ChatColor.GRAY + "/ip " + settings.allCommand() + ChatColor.WHITE + " - 온라인 플레이어 GUI (관리자)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<>();
            base.add(settings.helpCommand());
            base.add(settings.checkCommand());
            base.add("ban");
            base.add("pardon");
            base.add("status");
            base.add("banlist");
            base.add(settings.allCommand());
            return filter(base, args[0]);
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase(Locale.ROOT);
            if (sub.equals(settings.checkCommand()) || sub.equals("ban") || sub.equals("pardon")) {
                List<String> names = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    names.add(player.getName());
                }
                names.addAll(List.of("US", "UK", "RU", "KR", "JP", "CN"));
                return filter(names, args[1]);
            }
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(value);
            }
        }
        return result;
    }

    private boolean isCountryCode(String value) {
        return value.matches("^[A-Za-z]{2}$");
    }

    private boolean isIp(String value) {
        return value.matches(
                "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    }

    private String getPlayerIp(Player player) {
        InetSocketAddress address = player.getAddress();
        if (address == null || address.getAddress() == null) {
            return null;
        }
        return address.getAddress().getHostAddress();
    }
}
