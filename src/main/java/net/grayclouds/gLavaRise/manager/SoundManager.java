package net.grayclouds.gLavaRise.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.GLavaRise;

public class SoundManager {
    private final Plugin plugin;
    private final ConfigManager configManager;

    public SoundManager(Plugin plugin) {
        this.plugin = plugin;
        this.configManager = ((GLavaRise)plugin).getConfigManager();
    }

    public void playSound(String type, Player player) {
        var config = configManager.getConfig("sounds.yml");
        if (!config.getBoolean("SOUNDS." + type + ".enabled", true)) {
            return;
        }

        try {
            String soundName = config.getString("SOUNDS." + type + ".sound", "BLOCK_NOTE_BLOCK_PLING");
            float volume = (float) config.getDouble("SOUNDS." + type + ".volume", 1.0);
            float pitch = (float) config.getDouble("SOUNDS." + type + ".pitch", 1.0);

            @SuppressWarnings("deprecation")
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configuration for type: " + type);
        }
    }

    public void playSoundToAll(String type) {
        var config = configManager.getConfig("sounds.yml");
        if (!config.getBoolean("SOUNDS." + type + ".enabled", true)) {
            return;
        }

        try {
            String soundName = config.getString("SOUNDS." + type + ".sound", "BLOCK_NOTE_BLOCK_PLING");
            float volume = (float) config.getDouble("SOUNDS." + type + ".volume", 1.0);
            float pitch = (float) config.getDouble("SOUNDS." + type + ".pitch", 1.0);

            @SuppressWarnings("deprecation")
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid sound configuration for type: " + type);
        }
    }

    public void playCountdownSound(int secondsLeft, Player player) {
        String soundPath = secondsLeft <= 3 ? "countdown.final" : "countdown.tick";
        playSound(soundPath, player);
    }

    public void playGameStartSound(Player player) {
        playSound("game.start", player);
    }

    public void playGameEndSound(Player player) {
        playSound("game.end", player);
    }

    public void playWinSound(Player player) {
        playSound("game.win", player);
    }

    public void playLoseSound(Player player) {
        playSound("game.lose", player);
    }

    public void playHeightWarningSound(Player player) {
        playSound("height.warning", player);
    }

    public void playBorderWarningSound(Player player) {
        playSound("border.warning", player);
    }

    public void playTeamJoinSound(Player player) {
        playSound("team.join", player);
    }

    public void playTeamLeaveSound(Player player) {
        playSound("team.leave", player);
    }

    public void playErrorSound(Player player) {
        playSound("error", player);
    }
} 