package net.trysmp.tpa;

import lombok.Getter;
import net.trysmp.tpa.command.*;
import net.trysmp.tpa.listener.PlayerQuitListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class TryTpa extends JavaPlugin {

    @Getter
    private static TryTpa instance;

    @Override
    public void onEnable() {

        saveDefaultConfig();
        instance = this;

        new RemoveTpaAllCommand();
        new TpaCommand();
        new TpaAcceptCommand();
        new TpaHereCommand();
        new TpaHereAcceptCommand();
        new TpaAllCommand();

        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), this);

    }

}
