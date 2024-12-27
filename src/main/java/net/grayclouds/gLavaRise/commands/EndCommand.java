package net.grayclouds.gLavaRise.commands;

import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.manager.GameStateManager;
import net.grayclouds.gLavaRise.manager.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.events.GameEndEvent;
import net.grayclouds.gLavaRise.GLavaRise;
import org.bukkit.World;

public class EndCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final Plugin plugin;
    private final GameStateManager gameStateManager;
    private final PlayerManager playerManager;

    public EndCommand(LavaListener lavaListener, Plugin plugin, 
                     GameStateManager gameStateManager, PlayerManager playerManager) {
        this.lavaListener = lavaListener;
        this.plugin = plugin;
        this.gameStateManager = gameStateManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        
        if (!player.hasPermission("glavarise.end")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (!gameStateManager.isGameRunning()) {
            player.sendMessage("§cNo game is currently running!");
            return true;
        }

        // Get world reference before ending game
        World world = gameStateManager.getActiveWorld();
        
        // Call game end event first
        plugin.getServer().getPluginManager().callEvent(new GameEndEvent(world));
        
        // Then end the game
        gameStateManager.endGame();
        lavaListener.resetLavaRise();
        playerManager.reset();
        
        // Delete the game world
        ((GLavaRise)plugin).getWorldManager().deleteCurrentGameWorld();
        
        player.sendMessage("§aGame ended successfully!");

        // Stop win condition checking
        ((GLavaRise)plugin).getWinConditionManager().stopChecking();

        return true;
    }
}
