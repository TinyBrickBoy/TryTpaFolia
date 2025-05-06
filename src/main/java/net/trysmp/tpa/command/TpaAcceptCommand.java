package net.trysmp.tpa.command;

import net.trysmp.tpa.TryTpa;
import net.trysmp.tpa.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TpaAcceptCommand implements CommandExecutor, TabCompleter {

    public TpaAcceptCommand() {
        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpaccept")).setExecutor(this);
        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpaccept")).setTabCompleter(this);

        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpaaccept")).setExecutor(this);
        Objects.requireNonNull(TryTpa.getInstance().getCommand("tpaaccept")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (!(player.hasPermission("trytpa.command.tpa"))) {
            player.sendMessage(MessageUtil.get("Messages.NoPermission"));
            return false;
        }

        if (args.length == 0) {
            TpaCommand.accept(player);
            return false;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("*")) {
                TpaCommand.acceptAll(player);
                return false;
            }
            TpaCommand.accept(player, args[0]);
            return false;
        }

        player.sendMessage(MessageUtil.get("Messages.CommandSyntax").replaceAll("%command%", "tpaccept <player / *>"));
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1 && sender instanceof Player player) {
            for (UUID uuid : TpaCommand.requests.keySet()) {
                Player target = Bukkit.getPlayer(uuid);
                if (TpaCommand.requests.get(uuid) == player.getUniqueId() && target != null) {
                    list.add(target.getName());
                }
            }
            list.add("*");
        }

        return list.stream().filter(content -> content.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).sorted().toList();
    }

}
