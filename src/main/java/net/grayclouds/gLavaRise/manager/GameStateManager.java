package net.grayclouds.gLavaRise.manager;

import net.grayclouds.gLavaRise.GLavaRise;
import net.grayclouds.gLavaRise.events.GameStartEvent;
import net.grayclouds.gLavaRise.events.GameEndEvent;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GameStateManager {
    private final Plugin plugin;
    private boolean gameRunning = false;
    private boolean gamePaused = false;
    private World activeWorld;
    private int gameTime = 0;
    private BukkitTask timeTask;

    public GameStateManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void startGame(World world) {
        if (gameRunning) return;
        
        this.activeWorld = world;
        this.gameRunning = true;
        this.gamePaused = false;
        this.gameTime = 0;

        // Start game time counter
        if (timeTask != null) {
            timeTask.cancel();
        }
        timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!gamePaused) {
                gameTime++;
            }
        }, 20L, 20L);

        // Setup scoreboard
        ((GLavaRise)plugin).getScoreboardManager().setupScoreboard();
        
        // Start win condition checking
        ((GLavaRise)plugin).getWinConditionManager().startChecking();

        // Call game start event
        GameStartEvent event = new GameStartEvent(world);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void endGame() {
        if (!gameRunning) return;

        gameRunning = false;
        gamePaused = false;

        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }

        // Stop win condition checking
        ((GLavaRise)plugin).getWinConditionManager().stopChecking();
        
        // Remove scoreboard from all players
        ((GLavaRise)plugin).getScoreboardManager().removeScoreboard();

        // Delete the game world and teleport players to lobby
        ((GLavaRise)plugin).getWorldManager().deleteCurrentGameWorld();

        // Call game end event
        if (activeWorld != null) {
            GameEndEvent event = new GameEndEvent(activeWorld);
            Bukkit.getPluginManager().callEvent(event);
        }

        activeWorld = null;
        gameTime = 0;
    }

    public void pauseGame() {
        gamePaused = true;
    }

    public void resumeGame() {
        gamePaused = false;
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public boolean isPaused() {
        return gamePaused;
    }

    public World getActiveWorld() {
        return activeWorld;
    }

    public int getGameTime() {
        return gameTime;
    }
} 