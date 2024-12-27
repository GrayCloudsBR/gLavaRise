package net.grayclouds.gLavaRise.manager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.plugin.Plugin;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameplayManager implements Listener {
    private final Plugin plugin;
    private final ConfigManager configManager;
    private final GameStateManager gameStateManager;
    private final Set<Material> allowedBreakBlocks = new HashSet<>();
    private final Set<Material> allowedPlaceBlocks = new HashSet<>();
    private long gameStartTime;
    private boolean pvpEnabled;
    private int gracePeriod;
    private boolean fallDamage;
    private boolean allowCrafting;
    private boolean allowBuilding;

    public GameplayManager(Plugin plugin, ConfigManager configManager, GameStateManager gameStateManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gameStateManager = gameStateManager;
        loadConfig();
    }

    private void loadConfig() {
        var config = configManager.getConfig("game.yml");
        var gameplay = config.getConfigurationSection("GAME.gameplay");
        
        if (gameplay != null) {
            pvpEnabled = gameplay.getBoolean("pvp.enabled", true);
            gracePeriod = gameplay.getInt("pvp.grace-period", 30);
            fallDamage = gameplay.getBoolean("fall-damage", true);
            allowCrafting = gameplay.getBoolean("allow-crafting", true);
            allowBuilding = gameplay.getBoolean("allow-building", true);

            // Load block whitelists
            loadMaterials(gameplay.getStringList("block-break.whitelist"), allowedBreakBlocks);
            loadMaterials(gameplay.getStringList("block-place.whitelist"), allowedPlaceBlocks);
        }
    }

    private void loadMaterials(List<String> materials, Set<Material> target) {
        target.clear();
        for (String mat : materials) {
            try {
                target.add(Material.valueOf(mat.toUpperCase()));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in config: " + mat);
            }
        }
    }

    public void onGameStart() {
        gameStartTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!gameStateManager.isGameRunning()) return;
        
        if (!allowBuilding || (allowedBreakBlocks.size() > 0 && !allowedBreakBlocks.contains(event.getBlock().getType()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!gameStateManager.isGameRunning()) return;
        
        if (!allowBuilding || (allowedPlaceBlocks.size() > 0 && !allowedPlaceBlocks.contains(event.getBlock().getType()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!gameStateManager.isGameRunning()) return;
        
        if (!allowCrafting) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!gameStateManager.isGameRunning() || !(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        if (!pvpEnabled || (System.currentTimeMillis() - gameStartTime) / 1000 < gracePeriod) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!gameStateManager.isGameRunning() || !(event.getEntity() instanceof Player)) {
            return;
        }

        if (!fallDamage && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setCancelled(true);
        }
    }
} 