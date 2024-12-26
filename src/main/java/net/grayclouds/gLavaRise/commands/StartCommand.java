package net.grayclouds.gLavaRise.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.handler.WorldBorderHandler;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class StartCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final Plugin plugin;

    public StartCommand(LavaListener lavaListener, Plugin plugin) {
        this.lavaListener = lavaListener;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();
        String playerOnlyMessage = config.getString("CONFIG.MESSAGES.player-only", "This command can only be used by players!");
        String noPermissionMessage = config.getString("CONFIG.MESSAGES.no-permission", "You don't have permission to use this command!");
        String startMessage = config.getString("CONFIG.MESSAGES.lava-start", "Lava rise has started!");

        if (!(sender instanceof Player)) {
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("glavarise.start")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        World world = player.getWorld();
        
        // Set up world border centered on the player who starts the game
        WorldBorderHandler.setupWorldBorder(world, player.getLocation());
        
        lavaListener.startLavaRise(world);
        player.sendMessage(startMessage);
        return true;
    }
}
