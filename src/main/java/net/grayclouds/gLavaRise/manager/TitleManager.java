package net.grayclouds.gLavaRise.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.time.Duration;

public class TitleManager {
    private final Plugin plugin;
    private final boolean enabled;

    public TitleManager(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("CONFIG.TITLES.enabled", true);
    }

    public void showTitle(String event, Player player) {
        if (!enabled) return;

        String titleText = plugin.getConfig().getString("CONFIG.TITLES.events." + event + ".title", "");
        String subtitleText = plugin.getConfig().getString("CONFIG.TITLES.events." + event + ".subtitle", "");
        
        // Replace placeholders
        titleText = replacePlaceholders(titleText, player);
        subtitleText = replacePlaceholders(subtitleText, player);

        int fadeIn = plugin.getConfig().getInt("CONFIG.TITLES.events." + event + ".fade-in", 10);
        int stay = plugin.getConfig().getInt("CONFIG.TITLES.events." + event + ".stay", 40);
        int fadeOut = plugin.getConfig().getInt("CONFIG.TITLES.events." + event + ".fade-out", 10);

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

    public void showTitleToAll(String event) {
        if (!enabled) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            showTitle(event, player);
        }
    }

    private String replacePlaceholders(String text, Player player) {
        return text.replace("%player%", player.getName())
                  .replace("%alive%", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
    }

    public void clearTitle(Player player) {
        player.clearTitle();
    }

    public void clearTitleForAll() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.clearTitle();
        }
    }
} 