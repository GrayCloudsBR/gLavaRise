package net.grayclouds.gLavaRise.manager;

import net.grayclouds.gLavaRise.events.PlayerWinEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class WinConditionManager {
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final GameStateManager gameStateManager;
    private BukkitTask checkTask;

    public WinConditionManager(Plugin plugin, PlayerManager playerManager, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.gameStateManager = gameStateManager;
    }

    public void startChecking() {
        if (checkTask != null) {
            checkTask.cancel();
        }

        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gameStateManager.isGameRunning() || gameStateManager.isPaused()) {
                return;
            }

            // Check for last player standing
            if (playerManager.getAlivePlayers().size() == 1) {
                Player winner = Bukkit.getPlayer(playerManager.getAlivePlayers().iterator().next());
                if (winner != null && winner.isOnline()) {
                    // Call win event
                    PlayerWinEvent winEvent = new PlayerWinEvent(winner);
                    Bukkit.getPluginManager().callEvent(winEvent);
                    
                    // End game
                    gameStateManager.endGame();
                }
            }
        }, 20L, 20L); // Check every second
    }

    public void stopChecking() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }
} 