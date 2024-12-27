package net.grayclouds.gLavaRise.manager;

import net.grayclouds.gLavaRise.GLavaRise;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AutoStartManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final WorldManager worldManager;
    private final Set<UUID> waitingPlayers = new HashSet<>();
    private BukkitTask countdownTask;
    private int countdownSeconds;
    private boolean isCountingDown = false;

    public AutoStartManager(Plugin plugin, ConfigManager configManager, WorldManager worldManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.worldManager = worldManager;
    }

    public void addPlayer(Player player) {
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        if (!gameConfig.getBoolean("GAME.auto-start.enabled", true)) {
            return;
        }

        waitingPlayers.add(player.getUniqueId());
        checkForStart();
    }

    public void removePlayer(Player player) {
        waitingPlayers.remove(player.getUniqueId());
        
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        if (gameConfig.getBoolean("GAME.auto-start.cancel-on-player-leave", true)) {
            cancelCountdown();
        }
    }

    private void checkForStart() {
        if (isCountingDown) return;

        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        int minPlayers = gameConfig.getInt("GAME.auto-start.min-players", 2);

        if (waitingPlayers.size() >= minPlayers) {
            startCountdown();
        }
    }

    private void startCountdown() {
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        countdownSeconds = gameConfig.getInt("GAME.auto-start.countdown-seconds", 30);
        isCountingDown = true;

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (countdownSeconds <= 0) {
                startGame();
                return;
            }

            if (countdownSeconds <= 10 || countdownSeconds % 10 == 0) {
                String message = "§eGame starting in " + countdownSeconds + " seconds!";
                for (UUID uuid : waitingPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.sendMessage(message);
                    }
                }
            }

            countdownSeconds--;
        }, 0L, 20L);
    }

    private void startGame() {
        cancelCountdown();
        
        // Create new world
        World gameWorld = worldManager.createNewGameWorld();
        if (gameWorld == null) {
            plugin.getLogger().severe("Failed to create new game world!");
            return;
        }

        // Teleport all waiting players to the new world
        Location spawn = gameWorld.getSpawnLocation();
        for (UUID uuid : waitingPlayers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.teleport(spawn);
            }
        }

        // Start the game
        GLavaRise glavaRise = (GLavaRise) plugin;
        glavaRise.getGameStateManager().startGame(gameWorld);
        glavaRise.getLavaListener().startLavaRise(gameWorld);
        glavaRise.getWinConditionManager().startChecking();

        // Clear waiting players
        waitingPlayers.clear();
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        isCountingDown = false;
        countdownSeconds = 0;
    }

    public void reset() {
        cancelCountdown();
        waitingPlayers.clear();
    }

    public boolean isWaiting(Player player) {
        return waitingPlayers.contains(player.getUniqueId());
    }

    public int getWaitingPlayerCount() {
        return waitingPlayers.size();
    }
} 