package net.trysmp.tpa.listener;

import net.trysmp.tpa.command.TpaAllCommand;
import net.trysmp.tpa.command.TpaCommand;
import net.trysmp.tpa.command.TpaHereCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void handle(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        TpaCommand.requests.remove(player.getUniqueId());
        TpaHereCommand.requests.remove(player.getUniqueId());
        TpaAllCommand.requests.remove(player.getUniqueId());
    }

}
