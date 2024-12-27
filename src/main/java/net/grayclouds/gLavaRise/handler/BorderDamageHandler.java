package net.grayclouds.gLavaRise.handler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import net.grayclouds.gLavaRise.GLavaRise;

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

        damageTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            World gameWorld = ((GLavaRise)plugin).getGameStateManager().getActiveWorld();
            if (gameWorld == null) return;
            
            WorldBorder border = gameWorld.getWorldBorder();
            Location center = border.getCenter();
            double size = border.getSize() / 2.0;
            
            for (Player player : gameWorld.getPlayers()) {
                Location playerLoc = player.getLocation();
                double x = Math.abs(playerLoc.getX() - center.getX()) - size;
                double z = Math.abs(playerLoc.getZ() - center.getZ()) - size;
                double distance = Math.max(Math.max(x, z), 0.0);
                
                if (distance > 0) {
                    // Scale damage based on distance
                    double damage = Math.min(distance * 0.2, 5.0); // 0.2 damage per block, max 5 damage
                    player.damage(damage);
                    
                    // Push player back towards border
                    double angle = Math.atan2(center.getZ() - playerLoc.getZ(), center.getX() - playerLoc.getX());
                    double pushStrength = Math.min(distance * 0.1, 1.0);
                    player.setVelocity(player.getVelocity().add(
                        new org.bukkit.util.Vector(
                            Math.cos(angle) * pushStrength,
                            0.2,
                            Math.sin(angle) * pushStrength
                        )
                    ));
                }
            }
        }, 10L, 10L); // Check every half second
    }

    public static void stop() {
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
    }
} 