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
    private int riseInterval;
    private BukkitRunnable lavaTask;
    private boolean replaceAllBlocks;
    private double borderSize;

    public LavaListener(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        this.riseInterval = config.getInt("CONFIG.RISE-INTERVAL", 15);  // Updated to match config
        this.replaceAllBlocks = config.getBoolean("CONFIG.LAVA.replace-all-blocks", true);
    }

    private void announceHeight(World world) {
        String heightMsg = plugin.getConfig().getString("CONFIG.MESSAGES.height-warning", "Current lava height: %height%")
                .replace("%height%", String.valueOf(currentHeight));
        for (Player player : world.getPlayers()) {
            player.sendMessage(heightMsg);
        }
    }

    public void startLavaRise(World world) {
        if (world == null) {
            return;
        }

        if (isRising) {
            return;
        }

        if (currentHeight < -64 || currentHeight > world.getMaxHeight()) {
            currentHeight = savedHeight != -64 ? savedHeight : -64;
        }

        borderSize = WorldBorderHandler.getBorderSize(world) / 2;
        if (borderSize <= 0) {
            return;
        }

        isRising = true;

        // Set up world border
        WorldBorderHandler.setupWorldBorder(world, world.getSpawnLocation());

        lavaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentHeight >= world.getMaxHeight() || !isRising) {
                    this.cancel();
                    isRising = false;
                    return;
                }

                announceHeight(world);

                // Get world border information
                Location center = WorldBorderHandler.getCenter(world);
                borderSize = WorldBorderHandler.getBorderSize(world) / 2;
                
                // Place lava at current height
                for (int x = (int) (center.getBlockX() - borderSize); x < center.getBlockX() + borderSize; x++) {
                    for (int z = (int) (center.getBlockZ() - borderSize); z < center.getBlockZ() + borderSize; z++) {
                        try {
                            if (replaceAllBlocks || world.getBlockAt(x, currentHeight, z).getType() == Material.AIR) {
                                world.getBlockAt(x, currentHeight, z).setType(Material.LAVA);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error placing lava at " + x + ", " + currentHeight + ", " + z);
                        }
                    }
                }

                currentHeight++;
            }
        };

        // Schedule the task to run every 'riseInterval' seconds
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
        currentHeight = -64;
        savedHeight = -64;
    }

    public boolean isRising() {
        return isRising;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }
}