package net.grayclouds.gLavaRise.manager;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;
import java.util.*;

public class WorldManager {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private World currentGameWorld;

    public WorldManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public World createNewGameWorld() {
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        String prefix = gameConfig.getString("GAME.world-management.world-prefix", "lavarise_");
        String worldName = prefix + System.currentTimeMillis();

        // Create new world
        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        World newWorld = creator.createWorld();

        if (newWorld != null) {
            // Basic world setup
            newWorld.setDifficulty(Difficulty.NORMAL);
            newWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            newWorld.setTime(6000); // Set to midday
            newWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            newWorld.setStorm(false);
            
            // Set proper world border
            WorldBorder border = newWorld.getWorldBorder();
            border.setCenter(newWorld.getSpawnLocation());
            border.setSize(200.0); // Default size, can be configured
            border.setWarningDistance(5);
            border.setWarningTime(15);
            
            currentGameWorld = newWorld;
            manageWorldBackups();
        }

        return newWorld;
    }

    private void manageWorldBackups() {
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        if (!gameConfig.getBoolean("GAME.world-management.delete-old-worlds", true)) {
            return;
        }

        String prefix = gameConfig.getString("GAME.world-management.world-prefix", "lavarise_");
        int maxBackups = gameConfig.getInt("GAME.world-management.max-backup-worlds", 5);

        // Get all worlds with our prefix
        File worldContainer = plugin.getServer().getWorldContainer();
        File[] worldDirs = worldContainer.listFiles((dir, name) -> name.startsWith(prefix));
        
        if (worldDirs != null && worldDirs.length > maxBackups) {
            // Sort by creation time
            Arrays.sort(worldDirs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            
            // Delete excess worlds
            for (int i = maxBackups; i < worldDirs.length; i++) {
                deleteWorld(worldDirs[i]);
            }
        }
    }

    private void deleteWorld(File worldDir) {
        if (worldDir.exists() && worldDir.isDirectory()) {
            // Unload the world first if it's loaded
            World world = plugin.getServer().getWorld(worldDir.getName());
            if (world != null) {
                plugin.getServer().unloadWorld(world, false);
            }

            // Delete the world directory
            deleteDirectory(worldDir);
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    public void teleportToLobby(Player player) {
        FileConfiguration gameConfig = configManager.getConfig("game.yml");
        if (!gameConfig.getBoolean("GAME.world-management.teleport-to-lobby", true)) {
            return;
        }

        String lobbyWorldName = gameConfig.getString("GAME.world-management.lobby-world", "world");
        World lobbyWorld = plugin.getServer().getWorld(lobbyWorldName);
        
        if (lobbyWorld == null) {
            plugin.getLogger().warning("Lobby world not found!");
            return;
        }

        Location lobbyLoc = new Location(
            lobbyWorld,
            gameConfig.getDouble("GAME.world-management.lobby-coordinates.x", 0),
            gameConfig.getDouble("GAME.world-management.lobby-coordinates.y", 100),
            gameConfig.getDouble("GAME.world-management.lobby-coordinates.z", 0),
            (float) gameConfig.getDouble("GAME.world-management.lobby-coordinates.yaw", 0),
            (float) gameConfig.getDouble("GAME.world-management.lobby-coordinates.pitch", 0)
        );

        player.teleport(lobbyLoc);
    }

    public void teleportToLobby(Collection<? extends Player> players) {
        for (Player player : players) {
            teleportToLobby(player);
        }
    }

    public World getCurrentGameWorld() {
        return currentGameWorld;
    }
} 