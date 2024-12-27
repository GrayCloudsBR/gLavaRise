package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.GLavaRise;
import net.grayclouds.gLavaRise.events.PlayerEliminationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

public class PlayerDeathListener implements Listener {
    private final Plugin plugin;

    public PlayerDeathListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        GLavaRise glavaRise = (GLavaRise) plugin;
        
        if (!glavaRise.getGameStateManager().isGameRunning()) {
            return;
        }

        // Remove player from alive players
        glavaRise.getPlayerManager().removePlayer(player.getUniqueId());
        
        // Call elimination event
        PlayerEliminationEvent eliminationEvent = new PlayerEliminationEvent(player);
        Bukkit.getPluginManager().callEvent(eliminationEvent);

        // Teleport to lobby
        glavaRise.getWorldManager().teleportToLobby(player);
        
        // Check for win condition
        if (glavaRise.getPlayerManager().getAlivePlayers().size() == 1) {
            Player winner = Bukkit.getPlayer(glavaRise.getPlayerManager().getAlivePlayers().iterator().next());
            if (winner != null) {
                glavaRise.getGameStateManager().endGame();
                winner.sendMessage("§6§lVictory! §fYou won the game!");
                
                // Teleport winner to lobby after a short delay
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    glavaRise.getWorldManager().teleportToLobby(winner);
                }, 60L); // 3 second delay
            }
        }
    }
} 