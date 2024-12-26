package net.grayclouds.gLavaRise.handler;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class WorldBorderHandler {
    private static Plugin plugin;
    private static BukkitRunnable shrinkTask;
    private static int initialSize;
    private static int shrinkAmount;
    private static int minimumSize;
    private static String shrinkMethod;
    private static int timeInterval;
    private static int playersThreshold;
    private static boolean shrinkEnabled;
    
    public static void init(Plugin p) {
        plugin = p;
        // Initialize with default values for OVERWORLD
        loadConfig(plugin.getServer().getWorlds().get(0));
    }

    private static void loadConfig(World world) {
        FileConfiguration config = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType + ".BORDER";
        
        initialSize = config.getInt(basePath + ".initial-size", 250);
        shrinkEnabled = config.getBoolean(basePath + ".shrink-enabled", true);
        shrinkAmount = config.getInt(basePath + ".shrink.shrink-amount", 50);
        minimumSize = config.getInt(basePath + ".shrink.minimum-size", 50);
        shrinkMethod = config.getString(basePath + ".shrink.method", "TIME");
        timeInterval = config.getInt(basePath + ".shrink.time-interval", 300);
        playersThreshold = config.getInt(basePath + ".shrink.players-threshold", 5);
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
        if (world == null || center == null) return;

        // Load config for this specific world
        loadConfig(world);
        
        // Validate sizes
        validateBorderSize(world);

        WorldBorder border = world.getWorldBorder();
        
        // Set border size (no need to multiply by 2)
        border.setSize(initialSize);  // Remove the * 2
        border.setCenter(center);
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
        if (shrinkTask != null) {
            shrinkTask.cancel();
        }

        shrinkTask = new BukkitRunnable() {
            private int warningCountdown = 10; // 10 second warning

            @Override
            public void run() {
                WorldBorder border = world.getWorldBorder();
                double currentSize = border.getSize();

                if (currentSize <= minimumSize) {
                    String minMessage = plugin.getConfig().getString("CONFIG.MESSAGES.border-minimum", "The border has reached its minimum size!");
                    for (Player p : world.getPlayers()) {
                        p.sendMessage(minMessage);
                    }
                    this.cancel();
                    return;
                }

                // Warning countdown
                if (warningCountdown > 0) {
                    if (warningCountdown == 10 || warningCountdown <= 5) { // Warn at 10, 5, 4, 3, 2, 1 seconds
                        String warningMsg = plugin.getConfig().getString("CONFIG.MESSAGES.border-shrink-warning", 
                            "§c[Border] §fWill shrink by §e%amount% §fblocks in §c%time% §fseconds! (Current size: §e%size%§f)")
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

                // Shrink the border
                double newSize = Math.max(currentSize - shrinkAmount, minimumSize);
                border.setSize(newSize, 2); // Smooth transition over 2 seconds

                // Announce shrinking
                String shrinkMsg = plugin.getConfig().getString("CONFIG.MESSAGES.border-shrink", "The border is shrinking!");
                for (Player p : world.getPlayers()) {
                    p.sendMessage(shrinkMsg);
                }
            }
        };

        // Run task every timeInterval seconds
        shrinkTask.runTaskTimer(plugin, timeInterval * 20L, timeInterval * 20L);
    }

    public static void stop() {
        if (shrinkTask != null) {
            shrinkTask.cancel();
            shrinkTask = null;
        }
    }

    public static double getBorderSize(World world) {
        return world.getWorldBorder().getSize();
    }

    public static Location getCenter(World world) {
        return world.getWorldBorder().getCenter();
    }
}
