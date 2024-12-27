package net.grayclouds.gLavaRise.handler;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldBorderHandler {
    private static Plugin plugin;
    private static BukkitTask borderTask;
    private static int initialSize;
    private static int shrinkAmount;
    private static int minimumSize;
    private static int timeInterval;
    private static boolean shrinkEnabled;
    
    public static void init(Plugin plugin) {
        WorldBorderHandler.plugin = plugin;
    }

    private static void loadConfig(World world) {
        FileConfiguration config = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType + ".BORDER";
        
        initialSize = config.getInt(basePath + ".initial-size", 250);
        shrinkEnabled = config.getBoolean(basePath + ".shrink-enabled", true);
        shrinkAmount = config.getInt(basePath + ".shrink.shrink-amount", 50);
        minimumSize = config.getInt(basePath + ".shrink.minimum-size", 50);
        timeInterval = config.getInt(basePath + ".shrink.time-interval", 300);
    }

    private static String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "NETHER";
            case THE_END:
                return "END";
            default:
                return "OVERWORLD";
        }
    }

    public static void setupWorldBorder(World world, Location center) {
        if (plugin == null) {
            throw new IllegalStateException("WorldBorderHandler not initialized!");
        }
        
        if (world == null) return;

        // Load config for this specific world
        loadConfig(world);
        
        // Validate sizes
        validateBorderSize(world);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(center);
        
        // Set border size (no need to multiply by 2)
        border.setSize(initialSize);  // Remove the * 2
        border.setWarningDistance(10);
        border.setWarningTime(5);

        // Only start shrinking if enabled
        if (shrinkEnabled) {
            startBorderShrinking(world);
        }
    }

    private static void validateBorderSize(World world) {
        double maxSize = 29999984; // Minecraft's max world border size
        if (initialSize <= 0 || initialSize > maxSize) {
            initialSize = Math.min(250, (int)maxSize);
            plugin.getLogger().warning("Invalid border size for " + world.getName() + ", setting to: " + initialSize);
        }
        if (minimumSize <= 0 || minimumSize > initialSize) {
            minimumSize = Math.min(50, initialSize);
            plugin.getLogger().warning("Invalid minimum border size, setting to: " + minimumSize);
        }
    }

    private static void startBorderShrinking(World world) {
        if (borderTask != null) {
            borderTask.cancel();
        }

        // Validate shrink settings
        if (shrinkAmount <= 0 || timeInterval <= 0) {
            plugin.getLogger().warning("Invalid shrink settings! Disabling border shrinking.");
            return;
        }

        borderTask = new BukkitRunnable() {
            private int warningCountdown = 10;

            @Override
            public void run() {
                WorldBorder border = world.getWorldBorder();
                double currentSize = border.getSize();

                // Don't shrink if below minimum
                if (currentSize <= minimumSize) {
                    String minMessage = plugin.getConfig().getString("CONFIG.MESSAGES.border-minimum");
                    if (minMessage != null) {
                        for (Player p : world.getPlayers()) {
                            p.sendMessage(minMessage);
                        }
                    }
                    this.cancel();
                    return;
                }

                // Warning countdown
                if (warningCountdown > 0) {
                    if (warningCountdown == 10 || warningCountdown <= 5) {
                        String warningMsg = plugin.getConfig().getString("CONFIG.MESSAGES.border-shrink-warning", 
                            "§cWarning: Border will shrink by %amount% blocks in %time% seconds!")
                            .replace("%amount%", String.valueOf(shrinkAmount))
                            .replace("%time%", String.valueOf(warningCountdown))
                            .replace("%size%", String.valueOf((int)currentSize));
                        
                        for (Player p : world.getPlayers()) {
                            p.sendMessage(warningMsg);
                        }
                    }
                    warningCountdown--;
                    return;
                }

                // Reset warning countdown for next shrink
                warningCountdown = 10;

                // Calculate new size with validation
                double newSize = Math.max(currentSize - shrinkAmount, minimumSize);
                if (newSize != currentSize) {
                    border.setSize(newSize, 2); // 2-second transition

                    // Announce shrinking
                    String shrinkMsg = plugin.getConfig().getString("CONFIG.MESSAGES.border-shrink");
                    if (shrinkMsg != null) {
                        for (Player p : world.getPlayers()) {
                            p.sendMessage(shrinkMsg);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, timeInterval * 20L);  // Convert seconds to ticks (20 ticks = 1 second)
    }

    public static void stop() {
        if (borderTask != null) {
            borderTask.cancel();
            borderTask = null;
        }
    }

    public static double getBorderSize(World world) {
        return world.getWorldBorder().getSize();
    }

    public static Location getCenter(World world) {
        return world.getWorldBorder().getCenter();
    }

    public static boolean isLocationWithinBorder(Location location) {
        if (location == null || location.getWorld() == null) return false;
        
        WorldBorder border = location.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2;
        double x = location.getX() - center.getX();
        double z = location.getZ() - center.getZ();
        return Math.abs(x) <= size && Math.abs(z) <= size;
    }
}
