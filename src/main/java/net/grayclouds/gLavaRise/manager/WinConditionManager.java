package net.grayclouds.gLavaRise.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.UUID;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;
import java.time.Duration;

public class WinConditionManager {
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final GameStateManager gameStateManager;
    private BukkitTask timeTask;
    private int timeLimit;

    public WinConditionManager(Plugin plugin, PlayerManager playerManager, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.gameStateManager = gameStateManager;
    }

    public void startChecking() {
        if (plugin.getConfig().getBoolean("CONFIG.GAME.win-conditions.time-limit.enabled")) {
            timeLimit = plugin.getConfig().getInt("CONFIG.GAME.win-conditions.time-limit.minutes", 30) * 60;
            startTimeCheck();
        }
        
        // Check other win conditions every second
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkWinConditions, 20L, 20L);
    }

    private void checkWinConditions() {
        if (!gameStateManager.isGameRunning() || gameStateManager.isPaused()) {
            return;
        }

        // Check last player standing
        if (plugin.getConfig().getBoolean("CONFIG.GAME.win-conditions.last-player-standing", true)) {
            if (playerManager.getAlivePlayerCount() == 1) {
                Player winner = Bukkit.getPlayer(playerManager.getAlivePlayers().iterator().next());
                if (winner != null) {
                    handleWin(winner);
                }
            }
        }

        // Check height reached
        if (plugin.getConfig().getBoolean("CONFIG.GAME.win-conditions.height-reached", false)) {
            int currentHeight = plugin.getConfig().getInt("CONFIG.WORLDS.OVERWORLD.HEIGHT.current");
            int endHeight = plugin.getConfig().getInt("CONFIG.WORLDS.OVERWORLD.HEIGHT.end");
            if (currentHeight >= endHeight) {
                handleHeightReachedWin();
            }
        }
    }

    private void startTimeCheck() {
        if (timeTask != null) {
            timeTask.cancel();
        }

        timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (timeLimit <= 0) {
                handleTimeLimit();
                timeTask.cancel();
            }
            timeLimit--;
        }, 20L, 20L);
    }

    private void handleWin(Player winner) {
        if (winner == null || !winner.isOnline()) return;

        // Broadcast win message to all players
        String winMessage = plugin.getConfig().getString("CONFIG.MESSAGES.game-win", "§6%player% won the game!")
                .replace("%player%", winner.getName());
        
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.sendMessage(winMessage);
            
            // Show title to all players
            String titleMain = plugin.getConfig().getString("CONFIG.TITLES.win.title", "§6Victory!")
                    .replace("%player%", winner.getName());
            String titleSub = plugin.getConfig().getString("CONFIG.TITLES.win.subtitle", "§f%player% won the game!")
                    .replace("%player%", winner.getName());
            
            player.showTitle(Title.title(
                Component.text(titleMain),
                Component.text(titleSub),
                Title.Times.times(
                    Duration.ofMillis(500),  // fade in
                    Duration.ofMillis(3500), // stay
                    Duration.ofMillis(1000)  // fade out
                )
            ));
        }

        // End the game
        gameStateManager.endGame();
    }

    private void handleHeightReachedWin() {
        Player highestPlayer = null;
        double highestY = Double.NEGATIVE_INFINITY;

        // Find the highest player among alive players only
        for (UUID playerId : playerManager.getAlivePlayers()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                double playerY = player.getLocation().getY();
                if (playerY > highestY) {
                    highestY = playerY;
                    highestPlayer = player;
                }
            }
        }

        if (highestPlayer != null) {
            handleWin(highestPlayer);
        }
    }

    private void handleTimeLimit() {
        // Find player with highest Y coordinate when time runs out
        handleHeightReachedWin();
    }

    public void stop() {
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }
    }
} 