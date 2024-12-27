package net.grayclouds.gLavaRise.manager;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Plugin plugin;
    private final Map<String, FileConfiguration> configs;
    private final String[] configFiles = {
        "config.yml",
        "game.yml",
        "worlds.yml",
        "messages.yml",
        "teams.yml",
        "sounds.yml",
        "titles.yml"
    };

    public ConfigManager(Plugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        reload();
    }

    public void reload() {
        // Save default configs if they don't exist
        for (String fileName : configFiles) {
            try {
                if (!new File(plugin.getDataFolder(), fileName).exists()) {
                    plugin.saveResource(fileName, false);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not save " + fileName + ": " + e.getMessage());
            }
        }

        // Load all configs
        for (String fileName : configFiles) {
            File configFile = new File(plugin.getDataFolder(), fileName);
            if (configFile.exists()) {
                configs.put(fileName, YamlConfiguration.loadConfiguration(configFile));
            } else {
                plugin.getLogger().warning("Config file " + fileName + " not found!");
            }
        }
    }

    public FileConfiguration getConfig(String fileName) {
        return configs.getOrDefault(fileName, plugin.getConfig());
    }

    public WorldConfig getWorldConfig(World world) {
        String worldType = getWorldType(world);
        FileConfiguration worldsConfig = getConfig("worlds.yml");
        String basePath = "WORLDS." + worldType;

        return new WorldConfig(
            worldsConfig.getBoolean(basePath + ".enabled", false),
            worldsConfig.getInt(basePath + ".RISE-INTERVAL", 15),
            worldsConfig.getString(basePath + ".RISE-TYPE", "LAVA"),
            worldsConfig.getInt(basePath + ".HEIGHT.start", -64),
            worldsConfig.getInt(basePath + ".HEIGHT.end", 320),
            worldsConfig.getInt(basePath + ".HEIGHT.announcement-interval", 5),
            worldsConfig.getInt(basePath + ".BORDER.initial-size", 250),
            worldsConfig.getBoolean(basePath + ".BORDER.shrink-enabled", true),
            worldsConfig.getInt(basePath + ".BORDER.shrink.time-interval", 300),
            worldsConfig.getInt(basePath + ".BORDER.shrink.shrink-amount", 50),
            worldsConfig.getInt(basePath + ".BORDER.shrink.minimum-size", 50)
        );
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

    public static class WorldConfig {
        public final boolean enabled;
        public final int riseInterval;
        public final String riseType;
        public final int startHeight;
        public final int endHeight;
        public final int announcementInterval;
        public final int initialBorderSize;
        public final boolean shrinkEnabled;
        public final int shrinkInterval;
        public final int shrinkAmount;
        public final int minimumSize;

        public WorldConfig(boolean enabled, int riseInterval, String riseType,
                         int startHeight, int endHeight, int announcementInterval,
                         int initialBorderSize, boolean shrinkEnabled,
                         int shrinkInterval, int shrinkAmount, int minimumSize) {
            this.enabled = enabled;
            this.riseInterval = riseInterval;
            this.riseType = riseType;
            this.startHeight = startHeight;
            this.endHeight = endHeight;
            this.announcementInterval = announcementInterval;
            this.initialBorderSize = initialBorderSize;
            this.shrinkEnabled = shrinkEnabled;
            this.shrinkInterval = shrinkInterval;
            this.shrinkAmount = shrinkAmount;
            this.minimumSize = minimumSize;
        }
    }
} 