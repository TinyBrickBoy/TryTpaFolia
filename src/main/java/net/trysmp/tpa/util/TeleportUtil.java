package net.trysmp.tpa.util;

import lombok.experimental.UtilityClass;
import net.trysmp.tpa.TryTpa;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class TeleportUtil {

    private static final HashMap<UUID, Location> move = new HashMap<>();

    public static void teleport(Player player, Location location) {
        player.closeInventory();

        if (TryTpa.getInstance().getConfig().getInt("Teleport.CoolDown") < 1 || player.hasPermission("trytpa.bypass.teleport")) {
            player.teleport(location);
            playSound(player, "Teleport.TeleportSound");
            return;
        }

        move.put(player.getUniqueId(), player.getLocation());

        AtomicInteger seconds = new AtomicInteger(6);
        UUID uuid = player.getUniqueId();

        // Folia: Verwende entity-basiertes wiederkehrendes Scheduling
        player.getScheduler().runAtFixedRate(TryTpa.getInstance(), (task) -> {
            seconds.decrementAndGet();

            if (Bukkit.getPlayer(uuid) == null) {
                move.remove(uuid);
                task.cancel();
                return;
            }

            Player currentPlayer = Bukkit.getPlayer(uuid);
            if (currentPlayer == null) {
                move.remove(uuid);
                task.cancel();
                return;
            }

            if (TryTpa.getInstance().getConfig().getBoolean("Teleport.CancelOnMove")) {
                Location moveLocation = move.get(uuid);
                if (moveLocation != null && moveLocation.distance(currentPlayer.getLocation()) > TryTpa.getInstance().getConfig().getDouble("Teleport.MaximumMoveDistance")) {
                    currentPlayer.sendMessage(MessageUtil.get("Teleport.CancelMessage"));
                    if (!(TryTpa.getInstance().getConfig().getString("Teleport.CancelTitle.Title").equalsIgnoreCase("")) || !(TryTpa.getInstance().getConfig().getString("Teleport.CancelTitle.SubTitle").equalsIgnoreCase(""))) {
                        currentPlayer.sendTitle(MessageUtil.get("Teleport.CancelTitle.Title"), MessageUtil.get("Teleport.CancelTitle.SubTitle"));
                    }
                    playSound(currentPlayer, "Teleport.CancelSound");
                    move.remove(uuid);
                    task.cancel();
                    return;
                }
            }

            switch (seconds.get()) {
                case 5, 4, 3, 2, 1 -> {
                    if (!(TryTpa.getInstance().getConfig().getString("Teleport.Message").equalsIgnoreCase(""))) {
                        currentPlayer.sendMessage(MessageUtil.get("Teleport.Message").replaceAll("%seconds%", String.valueOf(seconds.get())));
                    }
                    if (!(TryTpa.getInstance().getConfig().getString("Teleport.Actionbar").equalsIgnoreCase(""))) {
                        currentPlayer.sendActionBar(MessageUtil.get("Teleport.Actionbar").replaceAll("%seconds%", String.valueOf(seconds.get())));
                    }
                    if (!(TryTpa.getInstance().getConfig().getString("Teleport.Title.Title").equalsIgnoreCase("")) || !(TryTpa.getInstance().getConfig().getString("Teleport.Title.SubTitle").equalsIgnoreCase(""))) {
                        currentPlayer.sendTitle(MessageUtil.get("Teleport.Title.Title").replaceAll("%seconds%", String.valueOf(seconds.get())), MessageUtil.get("Teleport.Title.SubTitle").replaceAll("%seconds%", String.valueOf(seconds.get())));
                    }
                    playSound(currentPlayer, "Teleport.CoolDownSound");
                }
                case 0 -> {
                    currentPlayer.teleport(location);
                    playSound(currentPlayer, "Teleport.TeleportSound");
                    move.remove(uuid);
                    task.cancel();
                }
            }
        }, null, 0, 20);
    }

    private static void playSound(Player player, String key) {
        String sound = TryTpa.getInstance().getConfig().getString(key, "");
        if (!sound.equalsIgnoreCase("")) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(sound), 5, 5);
            } catch (Exception ignored) { }
        }
    }
}