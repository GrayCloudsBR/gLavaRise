package net.grayclouds.gLavaRise.manager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.Map;

public class EffectManager {
    private final Plugin plugin;
    private final ConfigManager configManager;

    public EffectManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void applyWorldEffects(Player player, World world) {
        String worldType = world.getEnvironment().name();
        ConfigurationSection worldConfig = configManager.getConfig("worlds.yml")
            .getConfigurationSection("WORLDS." + worldType);

        if (worldConfig == null || !worldConfig.getBoolean("effects.enabled", false)) {
            return;
        }

        List<?> rawEffects = worldConfig.getMapList("effects.list");
        for (Object obj : rawEffects) {
            if (!(obj instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> effect = (Map<String, Object>) obj;
            try {
                String effectName = (String) effect.get("effect");
                Object ampObj = effect.getOrDefault("amplifier", 0);
                Object durObj = effect.getOrDefault("duration", -1);
                int amplifier = (ampObj instanceof Number) ? ((Number) ampObj).intValue() : 0;
                int duration = (durObj instanceof Number) ? ((Number) durObj).intValue() : -1;

                try {
                    @SuppressWarnings("deprecation")
                    PotionEffectType type = PotionEffectType.getByName(effectName.toUpperCase());
                    if (type != null) {
                        PotionEffect potionEffect = new PotionEffect(
                            type,
                            duration == -1 ? Integer.MAX_VALUE : duration * 20,
                            amplifier,
                            true,
                            false
                        );
                        player.addPotionEffect(potionEffect);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid effect configuration: " + effect);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid effect configuration: " + effect);
            }
        }
    }

    public void removeAllEffects(Player player) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
    }
} 