package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.events.*;
import net.grayclouds.gLavaRise.manager.*;
import net.grayclouds.gLavaRise.GLavaRise;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class GameEventListener implements Listener {
    private final Plugin plugin;
    private final SoundManager soundManager;
    private final TitleManager titleManager;
    private final ScoreboardManager scoreboardManager;
    private final WorldManager worldManager;

    public GameEventListener(Plugin plugin, SoundManager soundManager, 
                           TitleManager titleManager, ScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.soundManager = soundManager;
        this.titleManager = titleManager;
        this.scoreboardManager = scoreboardManager;
        this.worldManager = ((GLavaRise)plugin).getWorldManager();
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        soundManager.playSoundToAll("game.start");
        
        // Apply world effects to all players
        for (Player player : event.getWorld().getPlayers()) {
            ((GLavaRise)plugin).getEffectManager().applyWorldEffects(player, event.getWorld());
        }
        
        // Start gameplay tracking
        ((GLavaRise)plugin).getGameplayManager().onGameStart();
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        soundManager.playSoundToAll("game.end");
        
        // Remove effects from all players
        for (Player player : event.getWorld().getPlayers()) {
            ((GLavaRise)plugin).getEffectManager().removeAllEffects(player);
        }
        
        // Teleport all players to lobby
        worldManager.teleportToLobby(event.getWorld().getPlayers());
    }

    @EventHandler
    public void onPlayerElimination(PlayerEliminationEvent event) {
        Player player = event.getPlayer();
        soundManager.playSound("elimination", player);
        titleManager.showTitle("elimination", player);
        scoreboardManager.updateScoreboard();
    }

    @EventHandler
    public void onGamePause(GamePauseEvent event) {
        if (event.isPaused()) {
            soundManager.playSoundToAll("pause");
        } else {
            soundManager.playSoundToAll("resume");
        }
        scoreboardManager.updateScoreboard();
    }

    @EventHandler
    public void onPlayerWin(PlayerWinEvent event) {
        soundManager.playSoundToAll("game.win");
        
        // Teleport all players to lobby after a short delay
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            worldManager.teleportToLobby(event.getWinner().getWorld().getPlayers());
        }, 100L); // 5-second delay
    }
} 