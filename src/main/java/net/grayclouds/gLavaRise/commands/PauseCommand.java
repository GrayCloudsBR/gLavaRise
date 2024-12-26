package net.grayclouds.gLavaRise.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.listener.LavaListener;

public class PauseCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final Plugin plugin;

    public PauseCommand(LavaListener lavaListener, Plugin plugin) {
        this.lavaListener = lavaListener;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        String playerOnlyMessage = config.getString("CONFIG.MESSAGES.player-only", "This command can only be used by players!");
        String noPermissionMessage = config.getString("CONFIG.MESSAGES.no-permission", "You don't have permission to use this command!");
        String notActiveMessage = config.getString("CONFIG.MESSAGES.lava-not-active", "Lava rise is not currently active!");
        String pauseMessage = config.getString("CONFIG.MESSAGES.lava-pause", "The %type% rise has been paused at height: %height%!")
            .replace("%type%", lavaListener.getRiseTypeName())
            .replace("%height%", String.valueOf(lavaListener.getCurrentHeight()));

        if (!(sender instanceof Player)) {
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("glavarise.pause")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        if (!lavaListener.isRising()) {
            player.sendMessage(notActiveMessage);
            return true;
        }

        lavaListener.stopLavaRise();
        player.sendMessage(pauseMessage);
        return true;
    }
}