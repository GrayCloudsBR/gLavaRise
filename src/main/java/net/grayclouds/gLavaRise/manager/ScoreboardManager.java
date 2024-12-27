package net.grayclouds.gLavaRise.manager;

import net.grayclouds.gLavaRise.GLavaRise;
import net.grayclouds.gLavaRise.listener.LavaListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;

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
        if (scoreboard != null) {
            scoreboard.getObjectives().forEach(obj -> obj.unregister());
        }
        
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective("lavarise", Criteria.DUMMY, Component.text("§6§lLava Rise"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        // Apply to all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(scoreboard);
        }
        
        // Initial update
        updateScoreboard();
        
        // Start regular updates
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
        
        // Clear all scores first
        if (scoreboard != null) {
            for (String entry : scoreboard.getEntries()) {
                scoreboard.resetScores(entry);
            }
        }
        
        // Reset players to main scoreboard
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        // Clean up objective and scoreboard
        if (objective != null) {
            objective.unregister();
            objective = null;
        }
        scoreboard = null;
    }

    public void updateScoreboard() {
        if (scoreboard == null || !gameStateManager.isGameRunning()) return;
        
        // Clear all existing scores first
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        Objective objective = scoreboard.getObjective("lavarise");
        if (objective == null) {
            setupScoreboard();
            objective = scoreboard.getObjective("lavarise");
        }

        World gameWorld = gameStateManager.getActiveWorld();
        if (gameWorld != null) {
            LavaListener lavaListener = ((GLavaRise)plugin).getLavaListener();
            
            objective.getScore("§7§m----------------").setScore(7);
            objective.getScore("§fPlayers Alive: §a" + playerManager.getAlivePlayers().size()).setScore(6);
            objective.getScore("§fCurrent Height: §c" + lavaListener.getCurrentHeight()).setScore(5);
            objective.getScore("§fBorder: §c" + (int)gameWorld.getWorldBorder().getSize()).setScore(4);
            objective.getScore("§7§m----------------").setScore(3);
            
            long gameTime = gameStateManager.getGameTime();
            String timeStr = String.format("%02d:%02d", gameTime / 60, gameTime % 60);
            objective.getScore("§fTime: §e" + timeStr).setScore(2);
            objective.getScore("§7§m----------------").setScore(1);
        }
    }
} 