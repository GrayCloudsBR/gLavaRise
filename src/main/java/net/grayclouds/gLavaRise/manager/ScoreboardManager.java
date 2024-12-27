package net.grayclouds.gLavaRise.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.scoreboard.Criteria;
import java.util.Arrays;
import net.grayclouds.gLavaRise.GLavaRise;
import org.bukkit.scheduler.BukkitTask;

public class ScoreboardManager {
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final GameStateManager gameStateManager;
    private Scoreboard scoreboard;
    private Objective objective;
    private BukkitTask updateTask;

    public ScoreboardManager(Plugin plugin, PlayerManager playerManager, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.gameStateManager = gameStateManager;
    }

    public void setupScoreboard() {
        // Create new scoreboard
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        if (objective != null) {
            objective.unregister();
        }

        String title = plugin.getConfig().getString("CONFIG.WORLDS.OVERWORLD.SCOREBOARD.title", "§6§lLava Rise");
        objective = scoreboard.registerNewObjective("lavarise", Criteria.DUMMY, Component.text(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Initial update
        updateScoreboard();
        
        // Set scoreboard for all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }

        // Start update task
        if (updateTask != null) {
            updateTask.cancel();
        }
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateScoreboard, 20L, 20L);
    }

    public void removeScoreboard() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
        if (objective != null) {
            objective.unregister();
            objective = null;
        }
    }

    public void updateScoreboard() {
        if (objective == null || !gameStateManager.isGameRunning()) {
            return;
        }

        // Clear existing scores
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = plugin.getConfig().getStringList("CONFIG.WORLDS.OVERWORLD.SCOREBOARD.lines");
        if (lines.isEmpty()) {
            lines = getDefaultLines();
        }

        int score = lines.size();
        for (String line : lines) {
            String updatedLine = replacePlaceholders(line);
            objective.getScore(updatedLine).setScore(score--);
        }
    }

    private List<String> getDefaultLines() {
        return Arrays.asList(
            "§7§m----------------",
            "§fPlayers Alive: §a%alive%",
            "§fCurrent Height: §e%height%",
            "§fBorder: §c%border%",
            "§fTime: §b%time%",
            "§7§m----------------"
        );
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private String replacePlaceholders(String line) {
        int currentHeight = ((GLavaRise)plugin).getLavaListener().getCurrentHeight();
        double borderSize = gameStateManager.getActiveWorld() != null ? 
            gameStateManager.getActiveWorld().getWorldBorder().getSize() : 0;
            
        return line.replace("%alive%", String.valueOf(playerManager.getAlivePlayerCount()))
                  .replace("%height%", String.valueOf(currentHeight))
                  .replace("%border%", String.format("%.1f", borderSize))
                  .replace("%time%", formatTime(gameStateManager.getGameTime()));
    }
} 