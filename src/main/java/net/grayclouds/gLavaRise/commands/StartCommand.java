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

        World world = player.getWorld();
        String worldType = getWorldType(world);
        
        // Validate world configuration
        if (!config.getBoolean("CONFIG.WORLDS." + worldType + ".enabled", false)) {
            player.sendMessage("§cThis world type is not enabled in the config!");
            return true;
        }

        if (gameStateManager.isGameRunning()) {
            player.sendMessage("§cA game is already in progress!");
            return true;
        }

        // Reset player manager and add all players in the world
        playerManager.reset();
        for (Player p : world.getPlayers()) {
            playerManager.addPlayer(p);
        }

        try {
            WorldBorderHandler.setupWorldBorder(world, player.getLocation());
            gameStateManager.startGame(world);
            lavaListener.startLavaRise(world);
            ((GLavaRise)plugin).getWinConditionManager().startChecking();
            plugin.getServer().getPluginManager().callEvent(new GameStartEvent(world));
            
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

    private String getWorldType(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "NETHER";
            case THE_END:
                return "END";
            default:
                return "OVERWORLD";
        }
    }
}
