package net.grayclouds.gLavaRise.listener;

import net.grayclouds.gLavaRise.manager.ConfigManager;
import net.grayclouds.gLavaRise.manager.GameStateManager;
import net.grayclouds.gLavaRise.manager.PlayerManager;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.UUID;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.Listener;
import org.bukkit.block.Block;

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
        this.riseType = worldConfig.riseType;
        this.startHeight = world.getMinHeight();
        this.endHeight = world.getMaxHeight() - 10;
        this.heightAnnouncementInterval = worldConfig.announcementInterval;
        
        // Reset current height to start height
        this.currentHeight = this.startHeight;
        this.lastAnnouncedHeight = this.startHeight;
        
        plugin.getLogger().info("Loaded world config: start=" + startHeight + ", end=" + endHeight + ", interval=" + riseInterval);
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

        currentHeight = startHeight; // Initialize at start height
        lastAnnouncedHeight = currentHeight; // Initialize last announced height
        
        final int BLOCKS_PER_TICK = 1000; // Process 1000 blocks per tick to reduce lag
        final Location center = world.getWorldBorder().getCenter();
        final double borderSize = world.getWorldBorder().getSize() / 2;
        final int minX = (int) (center.getBlockX() - borderSize);
        final int maxX = (int) (center.getBlockX() + borderSize);
        final int minZ = (int) (center.getBlockZ() - borderSize);
        final int maxZ = (int) (center.getBlockZ() + borderSize);

        lavaTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!gameStateManager.isGameRunning() || gameStateManager.isPaused()) {
                    return;
                }

                if (currentHeight >= endHeight) {
                    this.cancel();
                    return;
                }

                int blocksProcessed = 0;
                for (int x = minX; x <= maxX && blocksProcessed < BLOCKS_PER_TICK; x++) {
                    for (int z = minZ; z <= maxZ && blocksProcessed < BLOCKS_PER_TICK; z++) {
                        Block block = world.getBlockAt(x, currentHeight, z);
                        if (block.getType() == Material.AIR) {
                            block.setType(Material.LAVA);
                            blocksProcessed++;
                        }
                    }
                }

                if (blocksProcessed < BLOCKS_PER_TICK) {
                    currentHeight++;
                    announceHeight(world);
                }
            }
        }.runTaskTimer(plugin, 0L, riseInterval);
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