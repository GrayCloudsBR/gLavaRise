package net.grayclouds.gLavaRise.handler;

import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;
import net.grayclouds.gLavaRise.manager.ConfigManager;

public class WorldBorderHandler {
    private static Plugin plugin;
    private static BukkitRunnable borderRunnable;

    public static void init(Plugin plugin) {
        WorldBorderHandler.plugin = plugin;
    }

    public static void setupWorldBorder(World world) {
        if (world == null) return;
        
        WorldBorder border = world.getWorldBorder();
        border.setCenter(world.getSpawnLocation());
        
        ConfigManager configManager = ((net.grayclouds.gLavaRise.GLavaRise)plugin).getConfigManager();
        ConfigManager.WorldConfig worldConfig = configManager.getWorldConfig(world);
        
        // Initialize border
        border.setSize(worldConfig.initialBorderSize);
        border.setWarningDistance(5);
        border.setWarningTime(15);
        border.setDamageAmount(2.0);
        
        // Setup border shrinking if enabled
        if (worldConfig.shrinkEnabled) {
            double totalShrinkBlocks = worldConfig.initialBorderSize - worldConfig.minimumSize;
            long shrinkTimeSeconds = (long)((totalShrinkBlocks / (double)worldConfig.shrinkAmount) * worldConfig.shrinkInterval);
            border.setSize(worldConfig.minimumSize, shrinkTimeSeconds);
        }
    }

    public static void stop() {
        if (borderRunnable != null) {
            borderRunnable.cancel();
            borderRunnable = null;
        }
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
}
