package net.grayclouds.gLavaRise.handler;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.FileConfiguration;

public class BorderDamageHandler {
    private static Plugin plugin;
    private static boolean enabled;
    private static double damageRadius;
    private static double damageAmount;
    private static int damageInterval;
    private static BukkitRunnable damageTask;
    private static String warningMessage;

    public static void init(Plugin p) {
        plugin = p;
        loadConfig();
        if (enabled) {
            startDamageTask();
        }
    }

    private static void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        enabled = config.getBoolean("CONFIG.BORDER-DAMAGE.enabled", true);
        damageRadius = config.getDouble("CONFIG.BORDER-DAMAGE.damage-radius", 5.0);
        damageAmount = config.getDouble("CONFIG.BORDER-DAMAGE.damage-amount", 2.0);
        damageInterval = config.getInt("CONFIG.BORDER-DAMAGE.damage-interval", 1);
        warningMessage = config.getString("CONFIG.MESSAGES.border-warning", "§cYou're too close to the border!");
    }

    private static void handlePlayerDeath(Player player) {
        String eliminatedMsg = plugin.getConfig().getString("CONFIG.MESSAGES.player-eliminated", "%player% has been eliminated!")
                .replace("%player%", player.getName());
        
        int playersRemaining = player.getWorld().getPlayers().size() - 1;
        String remainingMsg = plugin.getConfig().getString("CONFIG.MESSAGES.players-remaining", "%count% players remaining!")
                .replace("%count%", String.valueOf(playersRemaining));
        
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            p.sendMessage(eliminatedMsg);
            p.sendMessage(remainingMsg);
        }
    }

    private static void startDamageTask() {
        if (damageTask != null) {
            damageTask.cancel();
        }

        damageTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : plugin.getServer().getWorlds()) {
                    WorldBorder border = world.getWorldBorder();
                    double borderSize = border.getSize() / 2;
                    Location center = border.getCenter();

                    for (Player player : world.getPlayers()) {
                        if (player == null || !player.isOnline()) {
                            continue;
                        }
                        Location playerLoc = player.getLocation();
                        double distanceX = Math.abs(playerLoc.getX() - center.getX());
                        double distanceZ = Math.abs(playerLoc.getZ() - center.getZ());

                        // Check if player is within damage radius of border
                        if (distanceX > borderSize - damageRadius || 
                            distanceZ > borderSize - damageRadius) {
                            player.damage(damageAmount);
                            player.sendMessage(warningMessage);
                            
                            if (player.getHealth() <= damageAmount) {
                                handlePlayerDeath(player);
                            }
                        }
                    }
                }
            }
        };

        // Convert interval from seconds to ticks (20 ticks = 1 second)
        damageTask.runTaskTimer(plugin, 0L, damageInterval * 20L);
    }

    public static void stop() {
        if (damageTask != null) {
            damageTask.cancel();
            damageTask = null;
        }
    }

    public static void reload() {
        stop();
        loadConfig();
        if (enabled) {
            startDamageTask();
        }
    }
} 