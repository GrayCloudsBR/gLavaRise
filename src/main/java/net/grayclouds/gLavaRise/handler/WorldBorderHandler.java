package net.grayclouds.gLavaRise.handler;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

public class WorldBorderHandler {
    private static Plugin plugin;
    private static BukkitRunnable borderRunnable;

    public static void init(Plugin plugin) {
        WorldBorderHandler.plugin = plugin;
    }

    public static void setupWorldBorder(World world, Location center) {
        WorldBorder border = world.getWorldBorder();
        border.setCenter(center);
        
        FileConfiguration worldsConfig = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType + ".BORDER";
        
        double initialSize = worldsConfig.getDouble(basePath + ".initial-size", 200.0);
        border.setSize(initialSize);
        border.setDamageAmount(worldsConfig.getDouble(basePath + ".damage-amount", 1.0));
        border.setDamageBuffer(worldsConfig.getDouble(basePath + ".damage-radius", 5.0));
        border.setWarningDistance(5);
        border.setWarningTime(15);

        // Start border shrinking if enabled
        if (worldsConfig.getBoolean(basePath + ".shrink-enabled", true)) {
            startBorderShrinking(world);
        }
    }

    private static void startBorderShrinking(World world) {
        if (borderRunnable != null) {
            borderRunnable.cancel();
        }

        FileConfiguration worldsConfig = plugin.getConfig();
        String worldType = getWorldType(world);
        String basePath = "CONFIG.WORLDS." + worldType + ".BORDER.shrink";

        int timeInterval = worldsConfig.getInt(basePath + ".time-interval", 300);
        double shrinkAmount = worldsConfig.getDouble(basePath + ".shrink-amount", 50.0);
        double minimumSize = worldsConfig.getDouble(basePath + ".minimum-size", 50.0);

        WorldBorder border = world.getWorldBorder();
        borderRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                double currentSize = border.getSize();
                if (currentSize <= minimumSize) {
                    this.cancel();
                    return;
                }

                double newSize = Math.max(currentSize - shrinkAmount, minimumSize);
                border.setSize(newSize, timeInterval);

                // Announce border shrinking
                for (Player player : world.getPlayers()) {
                    player.sendMessage("§cThe border is shrinking!");
                    player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ELDER_GUARDIAN_CURSE, 0.5f, 1.0f);
                }
            }
        };
        borderRunnable.runTaskTimer(plugin, timeInterval * 20L, timeInterval * 20L);
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

    public static void stop() {
        if (borderRunnable != null) {
            borderRunnable.cancel();
            borderRunnable = null;
        }
    }
}
