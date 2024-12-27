package net.grayclouds.gLavaRise.handler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.grayclouds.gLavaRise.manager.PlayerManager;

public class BorderDamageHandler {
    private static Plugin plugin;
    private static PlayerManager playerManager;
    private static BukkitTask damageTask;
    private static double damageMultiplier = 1.0;

    public static void init(Plugin plugin, PlayerManager playerManager) {
        BorderDamageHandler.plugin = plugin;
        BorderDamageHandler.playerManager = playerManager;
        startDamageCheck();
    }

    private static void startDamageCheck() {
        if (damageTask != null) {
            damageTask.cancel();
        }

        boolean damageEnabled = plugin.getConfig().getBoolean("CONFIG.BORDER.damage-scaling.enabled", true);
        if (!damageEnabled) return;

        double baseAmount = plugin.getConfig().getDouble("CONFIG.WORLDS.OVERWORLD.BORDER-DAMAGE.damage-amount", 2.0);
        int interval = plugin.getConfig().getInt("CONFIG.WORLDS.OVERWORLD.BORDER-DAMAGE.damage-interval", 1);
        double multiplier = plugin.getConfig().getDouble("CONFIG.BORDER.damage-scaling.multiplier", 1.5);
        double maxDamage = plugin.getConfig().getDouble("CONFIG.BORDER.damage-scaling.max-damage", 10.0);

        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (!playerManager.isPlayerAlive(player)) continue;
                    
                    if (!WorldBorderHandler.isLocationWithinBorder(player.getLocation())) {
                        // Calculate scaled damage
                        double scaledDamage = Math.min(baseAmount * damageMultiplier, maxDamage);
                        player.damage(scaledDamage);
                        
                        // Increase multiplier for next hit
                        damageMultiplier *= multiplier;
                    } else {
                        // Reset multiplier when player is back inside border
                        damageMultiplier = 1.0;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, interval * 20L);
    }

    public static void stop() {
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
        damageMultiplier = 1.0;
    }

    public static void resetDamageMultiplier() {
        damageMultiplier = 1.0;
    }
} 