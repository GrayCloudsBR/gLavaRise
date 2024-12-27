package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.events.*;
import net.grayclouds.gLavaRise.manager.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class GameEventListener implements Listener {
    private final SoundManager soundManager;
    private final TitleManager titleManager;
    private final ScoreboardManager scoreboardManager;

    public GameEventListener(Plugin plugin, SoundManager soundManager, 
                           TitleManager titleManager, ScoreboardManager scoreboardManager) {
        this.soundManager = soundManager;
        this.titleManager = titleManager;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onGameStart(GameStartEvent event) {
        soundManager.playSoundToAll("game.start");
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        soundManager.playSoundToAll("game.end");
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
    }
} 