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
        
        // Ensure CONFIG and WORLDS sections exist
        if (!config.isConfigurationSection("CONFIG") || !config.isConfigurationSection("CONFIG.WORLDS")) {
            getLogger().severe("Missing main CONFIG or WORLDS section!");
            return false;
        }

        // Validate OVERWORLD section (required)
        if (!config.isConfigurationSection("CONFIG.WORLDS.OVERWORLD")) {
            getLogger().severe("Missing OVERWORLD section!");
            return false;
        }

        // Validate OVERWORLD settings
        String basePath = "CONFIG.WORLDS.OVERWORLD";
        if (!config.isBoolean(basePath + ".enabled")) {
            getLogger().warning("Missing enabled setting, setting default: true");
            config.set(basePath + ".enabled", true);
            needsSave = true;
        }

        if (!config.isInt(basePath + ".RISE-INTERVAL")) {
            getLogger().warning("Missing RISE-INTERVAL, setting default: 15");
            config.set(basePath + ".RISE-INTERVAL", 15);
            needsSave = true;
        }

        if (!config.isString(basePath + ".RISE-TYPE")) {
            getLogger().warning("Missing RISE-TYPE, setting default: LAVA");
            config.set(basePath + ".RISE-TYPE", "LAVA");
            needsSave = true;
        }

        // Validate BORDER section
        if (!config.isConfigurationSection(basePath + ".BORDER")) {
            getLogger().severe("Missing BORDER section!");
            return false;
        }

        // Validate BLOCK-SETTINGS section
        if (!config.isConfigurationSection(basePath + ".BLOCK-SETTINGS")) {
            getLogger().severe("Missing BLOCK-SETTINGS section!");
            return false;
        }

        // Validate BORDER-DAMAGE section
        if (!config.isConfigurationSection(basePath + ".BORDER-DAMAGE")) {
            getLogger().severe("Missing BORDER-DAMAGE section!");
            return false;
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