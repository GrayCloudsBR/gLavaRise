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
    private int currentHeight = -64;
    private int savedHeight = -64;
    private int startHeight = -64;
    private int endHeight = 320;
    private int riseInterval = 15;
    private BukkitRunnable lavaTask;
    private boolean replaceAllBlocks = true;
    private double borderSize;
    private String riseType = "LAVA";
    private World currentWorld;

    public LavaListener(Plugin plugin) {
        this.plugin = plugin;
        this.currentWorld = plugin.getServer().getWorlds().get(0);
        loadWorldConfig(currentWorld);
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
    }

    private void loadWorldConfig(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to load config for null world!");
            return;
        }

        FileConfiguration config = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType;
        
        this.riseInterval = config.getInt(basePath + ".RISE-INTERVAL", 15);
        this.replaceAllBlocks = config.getBoolean(basePath + ".BLOCK-SETTINGS.replace-all-blocks", true);
        this.riseType = config.getString(basePath + ".RISE-TYPE", "LAVA");
        
        // Load height settings
        this.startHeight = config.getInt(basePath + ".HEIGHT.start", -64);
        this.endHeight = config.getInt(basePath + ".HEIGHT.end", world.getMaxHeight());
        
        // Always set current height to start height when loading config
        this.currentHeight = this.startHeight;
        this.savedHeight = this.startHeight;
    }

    private void placeRisingBlock(World world, int x, int y, int z) {
        try {
            if (y >= world.getMinHeight() && y <= world.getMaxHeight()) {
                if (replaceAllBlocks || world.getBlockAt(x, y, z).getType() == Material.AIR) {
                    Material material = riseType.equals("VOID") ? Material.AIR : Material.LAVA;
                    world.getBlockAt(x, y, z).setType(material);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error placing block at " + x + "," + y + "," + z + ": " + e.getMessage());
        }
    }

    public String getRiseTypeName() {
        if (currentWorld != null) {
            loadWorldConfig(currentWorld);
        }
        return (riseType != null && riseType.equals("VOID")) ? "void" : "lava";
    }

    private void announceHeight(World world) {
        String heightMsg = plugin.getConfig().getString("CONFIG.MESSAGES.height-warning", 
            "§e[Rise] §fCurrent %type% height: §c%height%")
                .replace("%height%", String.valueOf(currentHeight))
                .replace("%type%", getRiseTypeName());
        for (Player player : world.getPlayers()) {
            player.sendMessage(heightMsg);
        }
    }

    public void startLavaRise(World world) {
        if (world == null) return;
        
        this.currentWorld = world;
        
        if (!isWorldEnabled(world)) {
            plugin.getLogger().warning("Rise is not enabled for " + getWorldType(world));
            return;
        }

        if (isRising) return;

        // Load config for this specific world
        loadWorldConfig(world);

        // No need for height validation here since we set it in loadWorldConfig
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
        if (currentWorld != null) {
            loadWorldConfig(currentWorld);
        }
        currentHeight = startHeight;
        savedHeight = startHeight;
    }

    public boolean isRising() {
        return isRising;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }
}