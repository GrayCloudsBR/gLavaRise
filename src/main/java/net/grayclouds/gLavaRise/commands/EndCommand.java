package net.grayclouds.gLavaRise.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.listener.LavaListener;

public class EndCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final Plugin plugin;

    public EndCommand(LavaListener lavaListener, Plugin plugin) {
        this.lavaListener = lavaListener;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        String playerOnlyMessage = config.getString("CONFIG.messages.player-only", "This command can only be used by players!");
        String noPermissionMessage = config.getString("CONFIG.messages.no-permission", "You don't have permission to use this command!");
        String endMessage = config.getString("CONFIG.messages.lava-end", "Lava rise has been ended!");

        if (!(sender instanceof Player)) {
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("glavarise.end")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        lavaListener.resetLavaRise();
        player.sendMessage(endMessage);
        return true;
    }
}
