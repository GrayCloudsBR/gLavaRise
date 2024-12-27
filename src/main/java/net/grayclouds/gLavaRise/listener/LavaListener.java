package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.manager.ConfigManager;
import net.grayclouds.gLavaRise.manager.GameStateManager;
import net.grayclouds.gLavaRise.manager.PlayerManager;
import net.grayclouds.gLavaRise.handler.WorldBorderHandler;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.Listener;

public class LavaListener implements Listener {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final GameStateManager gameStateManager;
    private final PlayerManager playerManager;
    
    private int currentHeight;
    private int startHeight;
    private int endHeight;
    private int riseInterval;
    private BukkitTask lavaTask;
    private boolean replaceAllBlocks;
    private String riseType;
    private int heightAnnouncementInterval;
    private int lastAnnouncedHeight;

    public LavaListener(Plugin plugin, ConfigManager configManager, 
                       GameStateManager gameStateManager, PlayerManager playerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameStateManager = gameStateManager;
        this.playerManager = playerManager;
    }

    private void loadWorldConfig(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to load config for null world!");
            return;
        }

        ConfigManager.WorldConfig worldConfig = configManager.getWorldConfig(world);
        
        this.riseInterval = worldConfig.riseInterval;
        this.replaceAllBlocks = true; // Could be added to WorldConfig if needed
        this.riseType = worldConfig.riseType;
        this.startHeight = worldConfig.startHeight;
        this.endHeight = worldConfig.endHeight;
        this.heightAnnouncementInterval = worldConfig.announcementInterval;
        
        // Reset current height to start height
        this.currentHeight = this.startHeight;
        this.lastAnnouncedHeight = this.startHeight;
    }

    private void placeRisingBlock(World world, int x, int y, int z) {
        try {
            if (y >= world.getMinHeight() && y <= world.getMaxHeight()) {
                Location loc = new Location(world, x, y, z);
                if (isWithinBorder(loc)) {
                    if (replaceAllBlocks || world.getBlockAt(x, y, z).getType() == Material.AIR) {
                        Material material = riseType.equals("VOID") ? Material.AIR : Material.LAVA;
                        world.getBlockAt(x, y, z).setType(material);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error placing block at " + x + "," + y + "," + z + ": " + e.getMessage());
        }
    }

    private boolean isWithinBorder(Location location) {
        return WorldBorderHandler.isLocationWithinBorder(location);
    }

    public String getRiseTypeName() {
        return (riseType != null && riseType.equals("VOID")) ? "void" : "lava";
    }

    private void announceHeight(World world) {
        if (Math.abs(currentHeight - lastAnnouncedHeight) >= heightAnnouncementInterval) {
            String heightMsg = plugin.getConfig().getString("CONFIG.MESSAGES.height-warning", 
                "§e[Rise] §fCurrent %type% height: §c%height%")
                    .replace("%height%", String.valueOf(currentHeight))
                    .replace("%type%", getRiseTypeName());
                    
            for (UUID playerId : playerManager.getAlivePlayers()) {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(heightMsg);
                }
            }
            lastAnnouncedHeight = currentHeight;
        }
    }

    public void startLavaRise(World world) {
        if (world == null || !gameStateManager.isGameRunning()) return;
        
        loadWorldConfig(world);
        
        if (lavaTask != null) {
            lavaTask.cancel();
        }

        lavaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameStateManager.isGameRunning() || gameStateManager.isPaused()) {
                    return;
                }

                if (currentHeight >= endHeight) {
                    this.cancel();
                    gameStateManager.endGame();
                    return;
                }

                announceHeight(world);

                Location center = world.getWorldBorder().getCenter();
                double borderSize = world.getWorldBorder().getSize() / 2;
                
                for (int x = (int) (center.getBlockX() - borderSize); x <= center.getBlockX() + borderSize; x++) {
                    for (int z = (int) (center.getBlockZ() - borderSize); z <= center.getBlockZ() + borderSize; z++) {
                        placeRisingBlock(world, x, currentHeight, z);
                    }
                }

                currentHeight++;
            }
        }.runTaskTimer(plugin, 0L, riseInterval * 20L);
    }

    public void pauseLavaRise() {
        if (gameStateManager.isGameRunning()) {
            gameStateManager.pauseGame();
        }
    }

    public void resumeLavaRise() {
        if (gameStateManager.isGameRunning() && gameStateManager.isPaused()) {
            gameStateManager.resumeGame();
        }
    }

    public void resetLavaRise() {
        if (lavaTask != null) {
            lavaTask.cancel();
            lavaTask = null;
        }
        
        if (gameStateManager.getActiveWorld() != null) {
            loadWorldConfig(gameStateManager.getActiveWorld());
        }
        
        gameStateManager.endGame();
        playerManager.reset();
    }

    public int getCurrentHeight() {
        return currentHeight;
    }
}