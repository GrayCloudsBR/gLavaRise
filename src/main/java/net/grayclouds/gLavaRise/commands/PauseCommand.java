package net.grayclouds.gLavaRise.commands;

import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.manager.GameStateManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.manager.ConfigManager;
import net.grayclouds.gLavaRise.GLavaRise;

public class PauseCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final GameStateManager gameStateManager;
    private final ConfigManager configManager;

    public PauseCommand(LavaListener lavaListener, Plugin plugin, GameStateManager gameStateManager) {
        this.lavaListener = lavaListener;
        this.gameStateManager = gameStateManager;
        this.configManager = ((GLavaRise)plugin).getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String playerOnlyMessage = configManager.getConfig("messages.yml").getString("MESSAGES.player-only", "This command can only be used by players!");
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("glavarise.pause")) {
            String noPermissionMessage = configManager.getConfig("messages.yml").getString("MESSAGES.no-permission", "You don't have permission!");
            player.sendMessage(noPermissionMessage);
            return true;
        }

        if (!gameStateManager.isGameRunning()) {
            String notRunningMessage = configManager.getConfig("messages.yml").getString("MESSAGES.game-not-running", "No game is currently running!");
            player.sendMessage(notRunningMessage);
            return true;
        }

        if (gameStateManager.isPaused()) {
            if (gameStateManager.resumeGame()) {
                lavaListener.resumeLavaRise();
                String resumeMessage = configManager.getConfig("messages.yml").getString("MESSAGES.game-resumed", "Game has been resumed!");
                player.sendMessage(resumeMessage);
            }
        } else {
            if (gameStateManager.pauseGame()) {
                lavaListener.pauseLavaRise();
                String pauseMessage = configManager.getConfig("messages.yml").getString("MESSAGES.game-paused", "Game has been paused!");
                player.sendMessage(pauseMessage);
            }
        }

        return true;
    }
}