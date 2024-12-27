package net.grayclouds.gLavaRise.commands;

import net.grayclouds.gLavaRise.events.GameStartEvent;
import net.grayclouds.gLavaRise.listener.LavaListener;
import net.grayclouds.gLavaRise.manager.GameStateManager;
import net.grayclouds.gLavaRise.manager.PlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import net.grayclouds.gLavaRise.handler.WorldBorderHandler;
import net.grayclouds.gLavaRise.GLavaRise;

public class StartCommand implements CommandExecutor {
    private final LavaListener lavaListener;
    private final Plugin plugin;
    private final GameStateManager gameStateManager;
    private final PlayerManager playerManager;

    public StartCommand(LavaListener lavaListener, Plugin plugin, 
                       GameStateManager gameStateManager, PlayerManager playerManager) {
        this.lavaListener = lavaListener;
        this.plugin = plugin;
        this.gameStateManager = gameStateManager;
        this.playerManager = playerManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin == null || !plugin.isEnabled()) {
            sender.sendMessage("§cPlugin is not properly initialized!");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        String playerOnlyMessage = config.getString("CONFIG.MESSAGES.player-only", "This command can only be used by players!");
        String noPermissionMessage = config.getString("CONFIG.MESSAGES.no-permission", "You don't have permission!");
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(playerOnlyMessage);
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("glavarise.start")) {
            player.sendMessage(noPermissionMessage);
            return true;
        }

        if (gameStateManager.isGameRunning()) {
            player.sendMessage("§cA game is already in progress!");
            return true;
        }

        // Create new game world
        World newWorld = ((GLavaRise)plugin).getWorldManager().createNewGameWorld();
        if (newWorld == null) {
            player.sendMessage("§cFailed to create game world!");
            return true;
        }

        // Reset player manager and add all players in the current world
        playerManager.reset();
        for (Player p : player.getWorld().getPlayers()) {
            playerManager.addPlayer(p.getUniqueId());
            p.teleport(newWorld.getSpawnLocation());
        }

        try {
            WorldBorderHandler.setupWorldBorder(newWorld);
            gameStateManager.startGame(newWorld);
            lavaListener.startLavaRise(newWorld);
            ((GLavaRise)plugin).getWinConditionManager().startChecking();
            plugin.getServer().getPluginManager().callEvent(new GameStartEvent(newWorld));
            
            String startMessage = config.getString("CONFIG.MESSAGES.lava-start", "The %type% is now rising!")
                .replace("%type%", lavaListener.getRiseTypeName());
            player.sendMessage(startMessage);
            
        } catch (Exception e) {
            player.sendMessage("§cAn error occurred while starting the game!");
            plugin.getLogger().severe("Error starting game: " + e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
}
