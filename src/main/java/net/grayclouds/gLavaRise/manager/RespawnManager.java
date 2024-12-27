package net.grayclouds.gLavaRise.manager;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RespawnManager {
    private final Plugin plugin;
    private final PlayerManager playerManager;
    private final Map<UUID, Integer> lives;
    private final boolean respawnEnabled;
    private final int maxLives;

    public RespawnManager(Plugin plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
        this.lives = new HashMap<>();
        this.respawnEnabled = plugin.getConfig().getBoolean("CONFIG.GAME.respawn.enabled", false);
        this.maxLives = plugin.getConfig().getInt("CONFIG.GAME.respawn.lives", 1);
    }

    public void initializePlayer(Player player) {
        if (respawnEnabled) {
            lives.put(player.getUniqueId(), maxLives);
            updateLivesDisplay(player);
        }
    }

    public boolean handleDeath(Player player) {
        if (!respawnEnabled) {
            playerManager.eliminatePlayer(player);
            return false;
        }

        UUID playerId = player.getUniqueId();
        int remainingLives = lives.getOrDefault(playerId, 0) - 1;
        lives.put(playerId, remainingLives);

        if (remainingLives <= 0) {
            playerManager.eliminatePlayer(player);
            return false;
        } else {
            respawnPlayer(player);
            updateLivesDisplay(player);
            return true;
        }
    }

    private void respawnPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        // You might want to add additional respawn logic here
        // like teleporting to a spawn point or giving starter items
    }

    private void updateLivesDisplay(Player player) {
        int remainingLives = lives.getOrDefault(player.getUniqueId(), 0);
        String message = plugin.getConfig().getString("CONFIG.MESSAGES.lives-remaining", "§e%lives% lives remaining!")
                .replace("%lives%", String.valueOf(remainingLives));
        player.sendMessage(message);
    }

    public void reset() {
        lives.clear();
    }

    public int getRemainingLives(Player player) {
        return lives.getOrDefault(player.getUniqueId(), 0);
    }
} 