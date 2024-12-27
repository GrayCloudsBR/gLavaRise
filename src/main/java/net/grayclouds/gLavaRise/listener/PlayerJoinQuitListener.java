package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.manager.AutoStartManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class PlayerJoinQuitListener implements Listener {
    private final Plugin plugin;
    private final AutoStartManager autoStartManager;

    public PlayerJoinQuitListener(Plugin plugin, AutoStartManager autoStartManager) {
        this.plugin = plugin;
        this.autoStartManager = autoStartManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Only add player if they're in the lobby world
        if (event.getPlayer().getWorld().getName().equals(
            plugin.getConfig().getString("GAME.world-management.lobby-world", "world"))) {
            autoStartManager.addPlayer(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        autoStartManager.removePlayer(event.getPlayer());
    }
} 