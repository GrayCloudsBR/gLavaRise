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
            
            // Set spawn location at a safe spot
            Location spawnLoc = findSafeSpawnLocation(newWorld);
            newWorld.setSpawnLocation(spawnLoc);
            
            // Initialize world border
            net.grayclouds.gLavaRise.handler.WorldBorderHandler.setupWorldBorder(newWorld);
            
            currentGameWorld = newWorld;
            manageWorldBackups();
        }

        return newWorld;
    }

    private Location findSafeSpawnLocation(World world) {
        FileConfiguration worldsConfig = configManager.getConfig("worlds.yml");
        int radius = worldsConfig.getInt("WORLDS.OVERWORLD.spawn.radius", 50);
        int minY = worldsConfig.getInt("WORLDS.OVERWORLD.spawn.min-y", 64);
        int maxY = worldsConfig.getInt("WORLDS.OVERWORLD.spawn.max-y", 100);
        boolean requireSafe = worldsConfig.getBoolean("WORLDS.OVERWORLD.spawn.safe-location", true);

        // Start from the center
        Location center = new Location(world, 0, maxY, 0);
        
        if (!requireSafe) {
            return center;
        }

        // Search for a safe location
        for (int y = maxY; y >= minY; y--) {
            for (int r = 0; r <= radius; r += 5) {
                for (int angle = 0; angle < 360; angle += 45) {
                    double x = r * Math.cos(Math.toRadians(angle));
                    double z = r * Math.sin(Math.toRadians(angle));
                    Location loc = new Location(world, x, y, z);
                    
                    if (isSafeLocation(loc)) {
                        return loc;
                    }
                }
            }
        }

        // If no safe location found, return the center at minY
        return new Location(world, 0, minY, 0);
    }

    private boolean isSafeLocation(Location loc) {
        return loc.getBlock().getType().isAir() &&
               loc.clone().add(0, 1, 0).getBlock().getType().isAir() &&
               !loc.clone().subtract(0, 1, 0).getBlock().getType().isAir();
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

    public void deleteCurrentGameWorld() {
        if (currentGameWorld != null) {
            String worldName = currentGameWorld.getName();
            
            // Teleport all players to lobby first
            teleportToLobby(currentGameWorld.getPlayers());
            
            // Unload the world
            plugin.getServer().unloadWorld(currentGameWorld, false);
            
            // Delete the world directory
            File worldDir = new File(plugin.getServer().getWorldContainer(), worldName);
            if (worldDir.exists()) {
                deleteDirectory(worldDir);
            }
            
            currentGameWorld = null;
        }
    }
} 