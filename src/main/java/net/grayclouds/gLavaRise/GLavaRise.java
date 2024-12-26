package net.grayclouds.gLavaRise;

import org.bukkit.plugin.java.JavaPlugin;
import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.commands.StartCommand;
import net.grayclouds.gLavaRise.commands.PauseCommand;
import net.grayclouds.gLavaRise.commands.EndCommand;
import net.grayclouds.gLavaRise.handler.WorldBorderHandler;
import net.grayclouds.gLavaRise.handler.BorderDamageHandler;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.File;

public final class GLavaRise extends JavaPlugin {
    private LavaListener lavaListener;

    @Override
    public void onEnable() {
        // First ensure config exists
        if (!new File(getDataFolder(), "config.yml").exists()) {
            getLogger().info("Config not found, creating default config.yml");
            saveDefaultConfig();
        }
        
        // Load and validate config
        reloadConfig();
        if (!validateConfig()) {
            getLogger().severe("Invalid configuration! Resetting to default values.");
            saveResource("config.yml", true);  // Force overwrite with default
            reloadConfig();
        }
        
        // Initialize handlers
        WorldBorderHandler.init(this);
        BorderDamageHandler.init(this);
        
        // Initialize LavaListener
        lavaListener = new LavaListener(this);
        
        // Register commands
        getCommand("startlava").setExecutor(new StartCommand(lavaListener, this));
        getCommand("pauselava").setExecutor(new PauseCommand(lavaListener, this));
        getCommand("endlava").setExecutor(new EndCommand(lavaListener, this));
    }

    private boolean validateConfig() {
        FileConfiguration config = getConfig();
        boolean needsSave = false;
        
        // Ensure CONFIG section exists
        if (!config.isConfigurationSection("CONFIG")) {
            getLogger().severe("Missing main CONFIG section!");
            return false;
        }

        // Validate RISE-INTERVAL
        if (!config.isInt("CONFIG.RISE-INTERVAL")) {
            getLogger().warning("Missing RISE-INTERVAL, setting default: 15");
            config.set("CONFIG.RISE-INTERVAL", 15);
            needsSave = true;
        }

        // Validate BORDER section
        if (!config.isConfigurationSection("CONFIG.BORDER")) {
            getLogger().severe("Missing BORDER section!");
            return false;
        }
        if (!config.isInt("CONFIG.BORDER.initial-size")) {
            getLogger().warning("Missing initial-size, setting default: 250");
            config.set("CONFIG.BORDER.initial-size", 250);
            needsSave = true;
        }
        if (!config.isBoolean("CONFIG.BORDER.shrink-enabled")) {
            getLogger().warning("Missing shrink-enabled, setting default: true");
            config.set("CONFIG.BORDER.shrink-enabled", true);
            needsSave = true;
        }

        // Validate LAVA section
        if (!config.isConfigurationSection("CONFIG.LAVA")) {
            getLogger().severe("Missing LAVA section!");
            return false;
        }
        if (!config.isBoolean("CONFIG.LAVA.replace-all-blocks")) {
            getLogger().warning("Missing replace-all-blocks, setting default: true");
            config.set("CONFIG.LAVA.replace-all-blocks", true);
            needsSave = true;
        }

        // Validate BORDER-DAMAGE section
        if (!config.isConfigurationSection("CONFIG.BORDER-DAMAGE")) {
            getLogger().severe("Missing BORDER-DAMAGE section!");
            return false;
        }
        if (!config.isBoolean("CONFIG.BORDER-DAMAGE.enabled")) {
            getLogger().warning("Missing border damage enabled, setting default: true");
            config.set("CONFIG.BORDER-DAMAGE.enabled", true);
            needsSave = true;
        }

        // Validate MESSAGES section
        if (!config.isConfigurationSection("CONFIG.MESSAGES")) {
            getLogger().severe("Missing MESSAGES section!");
            return false;
        }

        if (needsSave) {
            getLogger().info("Saving updated configuration...");
            saveConfig();
        }

        getLogger().info("Configuration validation successful!");
        return true;
    }

    @Override
    public void onDisable() {
        if (lavaListener != null) {
            lavaListener.stopLavaRise();
        }
        BorderDamageHandler.stop();
        WorldBorderHandler.stop();
    }
}