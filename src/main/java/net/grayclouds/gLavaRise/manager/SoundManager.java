package net.grayclouds.gLavaRise.manager;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SoundManager {
    private final Plugin plugin;

    public SoundManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void playSound(String configPath, Player player) {
        if (!plugin.getConfig().getBoolean("CONFIG.SOUNDS.enabled", true)) return;

        try {
            String soundPath = "CONFIG.SOUNDS." + configPath;
            if (!plugin.getConfig().contains(soundPath)) return;

            String soundKey = plugin.getConfig().getString(soundPath + ".type", "block.note_block.pling");
            Sound sound = Sound.BLOCK_NOTE_BLOCK_PLING; // Default sound
            try {
                @SuppressWarnings("deprecation")
                Sound newSound = Sound.valueOf(soundKey.toUpperCase().replace(".", "_"));
                sound = newSound;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound: " + soundKey);
            }

            float volume = (float) plugin.getConfig().getDouble(soundPath + ".volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble(soundPath + ".pitch", 1.0);

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (Exception e) {
            plugin.getLogger().warning("Error playing sound: " + configPath);
        }
    }

    public void playSoundToAll(String configPath) {
        if (!plugin.getConfig().getBoolean("CONFIG.SOUNDS.enabled", true)) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playSound(configPath, player);
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