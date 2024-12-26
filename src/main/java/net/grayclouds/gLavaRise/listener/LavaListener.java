package net.grayclouds.gLavaRise.listener;

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import net.grayclouds.gLavaRise.handler.WorldBorderHandler;
import org.bukkit.entity.Player;

public class LavaListener {
    private Plugin plugin;
    private boolean isRising = false;
    private int currentHeight;
    private int savedHeight;
    private int startHeight;
    private int endHeight;
    private int riseInterval;
    private BukkitRunnable lavaTask;
    private boolean replaceAllBlocks;
    private double borderSize;
    private String riseType;

    public LavaListener(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private boolean isWorldEnabled(World world) {
        String worldType = getWorldType(world);
        return plugin.getConfig().getBoolean("CONFIG.WORLDS." + worldType + ".enabled", false);
    }

    private String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "NETHER";
            case THE_END:
                return "END";
            default:
                return "OVERWORLD";
        }
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        // Remove default world loading here - we'll load per world
    }

    private void loadWorldConfig(World world) {
        FileConfiguration config = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType;
        
        this.riseInterval = config.getInt(basePath + ".RISE-INTERVAL", 15);
        this.replaceAllBlocks = config.getBoolean(basePath + ".BLOCK-SETTINGS.replace-all-blocks", true);
        this.riseType = config.getString(basePath + ".RISE-TYPE", "LAVA");
        
        // Load height settings
        this.startHeight = config.getInt(basePath + ".HEIGHT.start", -64);
        this.endHeight = config.getInt(basePath + ".HEIGHT.end", world.getMaxHeight());
        
        // Initialize current height if not set
        if (currentHeight < startHeight) {
            currentHeight = startHeight;
            savedHeight = startHeight;
        }
    }

    private void placeRisingBlock(World world, int x, int y, int z) {
        if (replaceAllBlocks || world.getBlockAt(x, y, z).getType() == Material.AIR) {
            Material material = riseType.equals("VOID") ? Material.AIR : Material.LAVA;
            world.getBlockAt(x, y, z).setType(material);
        }
    }

    public String getRiseTypeName() {
        return riseType.equals("VOID") ? "void" : "lava";
    }

    private void announceHeight(World world) {
        String heightMsg = plugin.getConfig().getString("CONFIG.MESSAGES.height-warning", "Current %type% height: %height%")
                .replace("%height%", String.valueOf(currentHeight))
                .replace("%type%", getRiseTypeName());
        for (Player player : world.getPlayers()) {
            player.sendMessage(heightMsg);
        }
    }

    public void startLavaRise(World world) {
        if (world == null) return;

        if (!isWorldEnabled(world)) {
            plugin.getLogger().warning("Rise is not enabled for " + getWorldType(world));
            return;
        }

        if (isRising) return;

        // Load config for this specific world
        loadWorldConfig(world);

        // Validate height bounds
        if (currentHeight < startHeight || currentHeight > endHeight) {
            currentHeight = savedHeight != startHeight ? savedHeight : startHeight;
        }

        borderSize = WorldBorderHandler.getBorderSize(world) / 2;
        if (borderSize <= 0) return;

        isRising = true;

        // Set up world border
        WorldBorderHandler.setupWorldBorder(world, world.getSpawnLocation());

        lavaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentHeight >= endHeight || !isRising) {
                    this.cancel();
                    isRising = false;
                    return;
                }

                announceHeight(world);

                // Get world border information
                Location center = WorldBorderHandler.getCenter(world);
                borderSize = WorldBorderHandler.getBorderSize(world) / 2;
                
                // Place blocks at current height
                for (int x = (int) (center.getBlockX() - borderSize); x < center.getBlockX() + borderSize; x++) {
                    for (int z = (int) (center.getBlockZ() - borderSize); z < center.getBlockZ() + borderSize; z++) {
                        try {
                            placeRisingBlock(world, x, currentHeight, z);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error placing " + riseType + " at " + x + ", " + currentHeight + ", " + z);
                        }
                    }
                }

                currentHeight++;
            }
        };

        lavaTask.runTaskTimer(plugin, 0L, riseInterval * 20L);
    }

    public void stopLavaRise() {
        if (lavaTask != null) {
            savedHeight = currentHeight;
            lavaTask.cancel();
        }
        isRising = false;
    }

    public void resetLavaRise() {
        if (lavaTask != null) {
            lavaTask.cancel();
        }
        isRising = false;
        currentHeight = startHeight;  // Use configured start height
        savedHeight = startHeight;
    }

    public boolean isRising() {
        return isRising;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }
}