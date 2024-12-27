package net.grayclouds.gLavaRise.manager;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Bukkit;
import net.grayclouds.gLavaRise.GLavaRise;
import net.grayclouds.gLavaRise.events.GameEndEvent;

/**
 * Manages the game state and coordinates other game-related managers.
 */
public class GameStateManager {
    private boolean isGameRunning;
    private boolean isPaused;
    private World activeWorld;
    private final Plugin plugin;
    private int gameTime;
    private BukkitTask timeTask;
    private final SoundManager soundManager;

    public GameStateManager(Plugin plugin) {
        this.plugin = plugin;
        this.isGameRunning = false;
        this.isPaused = false;
        this.gameTime = 0;
        
        // Initialize all managers
        this.soundManager = new SoundManager(plugin);
    }

    /**
     * Starts the game in the specified world.
     * @param world The world where the game will take place
     * @return true if the game started successfully, false otherwise
     */
    public boolean startGame(World world) {
        if (isGameRunning || world == null) {
            return false;
        }

        isGameRunning = true;
        isPaused = false;
        activeWorld = world;
        
        startGameTimer();
        soundManager.playSoundToAll("game.start");
        
        // Setup scoreboard
        if (plugin instanceof GLavaRise) {
            ((GLavaRise)plugin).getScoreboardManager().setupScoreboard();
        }
        
        return true;
    }

    /**
     * Pauses the current game.
     * @return true if the game was successfully paused, false otherwise
     */
    public boolean pauseGame() {
        if (!isGameRunning || isPaused) {
            return false;
        }
        isPaused = true;
        return true;
    }

    /**
     * Resumes the current game from pause.
     * @return true if the game was successfully resumed, false otherwise
     */
    public boolean resumeGame() {
        if (!isGameRunning || !isPaused) {
            return false;
        }
        isPaused = false;
        return true;
    }

    /**
     * Ends the current game and cleans up resources.
     * @return true if the game was successfully ended, false otherwise
     */
    public boolean endGame() {
        if (!isGameRunning) {
            return false;
        }
        
        isGameRunning = false;
        isPaused = false;
        stopGameTimer();
        
        if (plugin instanceof GLavaRise) {
            ((GLavaRise)plugin).getScoreboardManager().removeScoreboard();
        }
        
        if (activeWorld != null) {
            plugin.getServer().getPluginManager().callEvent(
                new GameEndEvent(activeWorld)
            );
            soundManager.playSoundToAll("game.end");
            activeWorld = null;
        }
        
        return true;
    }

    private void startGameTimer() {
        stopGameTimer(); // Ensure any existing timer is cancelled
        
        timeTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!isPaused && isGameRunning) {
                gameTime++;
            }
        }, 20L, 20L);
    }

    private void stopGameTimer() {
        if (timeTask != null) {
            timeTask.cancel();
            timeTask = null;
        }
        gameTime = 0;
    }

    // Getters
    public boolean isGameRunning() {
        return isGameRunning;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public World getActiveWorld() {
        return activeWorld;
    }

    public int getGameTime() {
        return gameTime;
    }
} 