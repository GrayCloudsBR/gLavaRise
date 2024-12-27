package net.grayclouds.gLavaRise.manager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import java.time.Duration;
import net.grayclouds.gLavaRise.GLavaRise;

public class TitleManager {
    private final ConfigManager configManager;

    public TitleManager(Plugin plugin) {
        this.configManager = ((GLavaRise)plugin).getConfigManager();
    }

    public void showTitle(String type, Player player) {
        var config = configManager.getConfig("titles.yml");
        if (!config.getBoolean("TITLES." + type + ".enabled", true)) {
            return;
        }

        String titleText = config.getString("TITLES." + type + ".title", "");
        String subtitleText = config.getString("TITLES." + type + ".subtitle", "");
        int fadeIn = config.getInt("TITLES." + type + ".fade-in", 10);
        int stay = config.getInt("TITLES." + type + ".stay", 70);
        int fadeOut = config.getInt("TITLES." + type + ".fade-out", 20);

        Title title = Title.title(
            Component.text(titleText),
            Component.text(subtitleText),
            Title.Times.times(
                Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L),
                Duration.ofMillis(fadeOut * 50L)
            )
        );

        player.showTitle(title);
    }
} 