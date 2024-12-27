package net.grayclouds.gLavaRise;

import net.grayclouds.gLavaRise.commands.*;
import net.grayclouds.gLavaRise.handler.*;
import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.manager.*;
import net.grayclouds.gLavaRise.listener.GameEventListener;
import net.grayclouds.gLavaRise.listener.PlayerDeathListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class GLavaRise extends JavaPlugin {
    private ConfigManager configManager;
    private GameStateManager gameStateManager;
    private PlayerManager playerManager;
    private LavaListener lavaListener;
    private SoundManager soundManager;
    private TitleManager titleManager;
    private ScoreboardManager scoreboardManager;
    private WinConditionManager winConditionManager;
    private TeamManager teamManager;
    private RespawnManager respawnManager;
    private WorldManager worldManager;
    private EffectManager effectManager;
    private GameplayManager gameplayManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (!validateConfig()) {
            getLogger().severe("Invalid configuration! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize handlers
        WorldBorderHandler.init(this);
        configManager = new ConfigManager(this);
        playerManager = new PlayerManager(this);
        BorderDamageHandler.init(this);

        worldManager = new WorldManager(this, configManager);
        gameStateManager = new GameStateManager(this);
        teamManager = new TeamManager(this);
        respawnManager = new RespawnManager(this, playerManager);
        winConditionManager = new WinConditionManager(this, playerManager, gameStateManager);
        soundManager = new SoundManager(this);
        titleManager = new TitleManager(this);
        scoreboardManager = new ScoreboardManager(this, playerManager, gameStateManager);
        effectManager = new EffectManager(this, configManager);
        gameplayManager = new GameplayManager(this, configManager, gameStateManager);
        lavaListener = new LavaListener(this, configManager, gameStateManager, playerManager);
        
        // Register commands
        getCommand("start").setExecutor(new StartCommand(lavaListener, this, gameStateManager, playerManager));
        getCommand("end").setExecutor(new EndCommand(lavaListener, this, gameStateManager, playerManager));
        getCommand("pause").setExecutor(new PauseCommand(lavaListener, this, gameStateManager));
        getCommand("reload").setExecutor(new ReloadCommand(this, configManager));
        getCommand("team").setExecutor(new TeamCommand(this, teamManager));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GameEventListener(this, soundManager, titleManager, scoreboardManager), this);
        getServer().getPluginManager().registerEvents(lavaListener, this);
        getServer().getPluginManager().registerEvents(gameplayManager, this);
    }

    @Override
    public void onDisable() {
        if (gameStateManager.isGameRunning()) {
            lavaListener.resetLavaRise();
            worldManager.teleportToLobby(getServer().getOnlinePlayers());
        }
        BorderDamageHandler.stop();
        WorldBorderHandler.stop();
        getLogger().info("GLavaRise has been disabled!");
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TitleManager getTitleManager() {
        return titleManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public WinConditionManager getWinConditionManager() {
        return winConditionManager;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public RespawnManager getRespawnManager() {
        return respawnManager;
    }

    public LavaListener getLavaListener() {
        return lavaListener;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public GameplayManager getGameplayManager() {
        return gameplayManager;
    }

    private boolean validateConfig() {
        FileConfiguration config = getConfig();
        boolean needsSave = false;
        
        // Add validation for HEIGHT.announcement-interval
        String[] worlds = {"OVERWORLD", "NETHER", "END"};
        for (String world : worlds) {
            String basePath = "CONFIG.WORLDS." + world;
            if (config.getBoolean(basePath + ".enabled", false)) {
                if (!config.isInt(basePath + ".HEIGHT.announcement-interval")) {
                    config.set(basePath + ".HEIGHT.announcement-interval", 5);
                    needsSave = true;
                }
                
                // Validate rise interval is positive
                int riseInterval = config.getInt(basePath + ".RISE-INTERVAL", 15);
                if (riseInterval <= 0) {
                    config.set(basePath + ".RISE-INTERVAL", 15);
                    needsSave = true;
                }
                
                // Validate height values
                int startHeight = config.getInt(basePath + ".HEIGHT.start");
                int endHeight = config.getInt(basePath + ".HEIGHT.end");
                if (startHeight >= endHeight) {
                    getLogger().severe("Invalid height configuration for " + world);
                    return false;
                }
            }
        }

        if (needsSave) {
            saveConfig();
        }

        return true;
    }
}