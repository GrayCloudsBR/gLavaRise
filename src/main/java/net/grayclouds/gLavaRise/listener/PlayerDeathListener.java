package net.grayclouds.gLavaRise.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.GLavaRise;

public class PlayerDeathListener implements Listener {
    private final Plugin plugin;

    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (((GLavaRise)plugin).getRespawnManager().handleDeath(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
} 