package net.trysmp.tpa.command;

import net.trysmp.tpa.TryTpa;
import net.trysmp.tpa.util.DateUtil;
import net.trysmp.tpa.util.MessageUtil;
import net.trysmp.tpa.util.TeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TpaHereCommand implements CommandExecutor, TabCompleter {

    private static final HashMap<UUID, Long> commandDelay = new HashMap<>();
    public static final HashMap<UUID, UUID> requests = new HashMap<>();

    public TpaHereCommand() {
        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpahere")).setExecutor(this);
        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpahere")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!(player.hasPermission("trytpa.command.tpahere"))) {
            player.sendMessage(MessageUtil.get("Messages.NoPermission"));
            return false;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            accept(player, args[1]);
            return false;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("accept")) {
            accept(player);
            return false;
        }

        long delay = commandDelay.getOrDefault(player.getUniqueId(), 0L);
        if (delay > System.currentTimeMillis()) {
            player.sendMessage(MessageUtil.get("Messages.CommandDelay").replaceAll("%time%", DateUtil.secondsToTime((delay - System.currentTimeMillis()) / 1000)));
            return false;
        }

        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(MessageUtil.get("Messages.PlayerNotFound"));
                return false;
            }

            if (player == target) {
                player.sendMessage(MessageUtil.get("Messages.NotYourself"));
                return false;
            }

            player.sendMessage(MessageUtil.get("Messages.Sent"));
            target.sendMessage(MessageUtil.getRequest("TpaHere", player.getName()));

            if (TryTpa.getInstance().getConfig().getBoolean("Settings.Sounds.TpaHere")) {
                target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5, 5);
            }

            if (!(player.hasPermission("trytpa.bypass.cooldown"))) {
                commandDelay.put(player.getUniqueId(), System.currentTimeMillis() + TryTpa.getInstance().getConfig().getLong("Settings.Cooldown.TpaHere"));

                // Folia: Verwende entity-basiertes Scheduling
                player.getScheduler().runDelayed(TryTpa.getInstance(), (task) -> {
                    requests.remove(player.getUniqueId());
                }, null, 20 * TryTpa.getInstance().getConfig().getLong("Settings.Expiration.TpaHere"));
            }

            requests.put(player.getUniqueId(), target.getUniqueId());
            return false;
        }

        player.sendMessage(MessageUtil.get("Messages.CommandSyntax").replaceAll("%command%", "tpahere <player>"));
        player.sendMessage(MessageUtil.get("Messages.CommandSyntax").replaceAll("%command%", "tpahere accept <player / *>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 2 && sender instanceof Player player && args[0].equalsIgnoreCase("accept")) {
            for (UUID uuid : requests.keySet()) {
                Player target = Bukkit.getPlayer(uuid);
                if (requests.get(uuid) == player.getUniqueId() && target != null) {
                    list.add(target.getName());
                }
            }
        }

        if (args.length == 1) {
            list.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            list.add("accept");
        }

        return list.stream().filter(content -> content.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().toList();
    }

    public static void accept(Player player) {
        for (UUID uuid : requests.keySet()) {
            Player target = Bukkit.getPlayer(uuid);
            if (requests.get(uuid) == player.getUniqueId() && target != null) {
                accept(player, target.getName());
                return;
            }
        }

        player.sendMessage(MessageUtil.get("Messages.NoRequests"));
    }

    public static void accept(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(MessageUtil.get("Messages.PlayerNotFound"));
            return;
        }

        if (player == target) {
            player.sendMessage(MessageUtil.get("Messages.NotYourself"));
            return;
        }

        UUID uuid = requests.get(target.getUniqueId());
        requests.remove(target.getUniqueId());

        if (uuid != null && uuid == player.getUniqueId()) {
            target.sendMessage(MessageUtil.get("Messages.AcceptedOther").replaceAll("%player%", player.getName()));
            player.sendMessage(MessageUtil.get("Messages.Accepted"));
            TeleportUtil.teleport(player, target.getLocation());
        } else {
            player.sendMessage(MessageUtil.get("Messages.Expired"));
        }
    }
}
