package net.grayclouds.gLavaRise.handler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;

public class BorderDamageHandler {
    private static Plugin plugin;
    private static BukkitTask damageTask;

    public static void init(Plugin plugin) {
        BorderDamageHandler.plugin = plugin;
        startDamageTask();
    }

    private static void startDamageTask() {
        if (damageTask != null) {
            damageTask.cancel();
        }

        int interval = plugin.getConfig().getInt("CONFIG.WORLDS.OVERWORLD.BORDER-DAMAGE.damage-interval", 1);
        damageTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (isOutsideBorder(player.getLocation())) {
                    damagePlayer(player);
                }
            }
        }, interval * 20L, interval * 20L);
    }

    private static boolean isOutsideBorder(Location location) {
        double size = location.getWorld().getWorldBorder().getSize() / 2.0;
        Location center = location.getWorld().getWorldBorder().getCenter();
        double x = location.getX() - center.getX();
        double z = location.getZ() - center.getZ();
        return Math.abs(x) > size || Math.abs(z) > size;
    }

    private static double getDistanceOutsideBorder(Location location) {
        double size = location.getWorld().getWorldBorder().getSize() / 2.0;
        Location center = location.getWorld().getWorldBorder().getCenter();
        double x = Math.abs(location.getX() - center.getX()) - size;
        double z = Math.abs(location.getZ() - center.getZ()) - size;
        return Math.max(Math.max(x, z), 0.0);
    }

    private static void damagePlayer(Player player) {
        double distance = getDistanceOutsideBorder(player.getLocation());
        double multiplier = plugin.getConfig().getDouble("CONFIG.BORDER.damage-scaling.multiplier", 1.5);
        double maxDamage = plugin.getConfig().getDouble("CONFIG.BORDER.damage-scaling.max-damage", 10.0);
        
        double damage = Math.min(distance * multiplier, maxDamage);
        player.damage(damage);
    }

    public static void stop() {
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
    }
} 