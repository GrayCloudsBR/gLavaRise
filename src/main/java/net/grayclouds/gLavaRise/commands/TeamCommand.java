package net.grayclouds.gLavaRise.commands;

import net.grayclouds.gLavaRise.manager.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TeamCommand implements CommandExecutor {
    private final Plugin plugin;
    private final TeamManager teamManager;

    public TeamCommand(Plugin plugin, TeamManager teamManager) {
        this.plugin = plugin;
        this.teamManager = teamManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /team join <teamname>");
                    return true;
                }
                teamManager.addPlayerToTeam(player, args[1]);
            }
            case "leave" -> teamManager.removePlayerFromTeam(player);
            case "list" -> listTeams(player);
            default -> sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6Team Commands:");
        player.sendMessage("§f/team join <team> §7- Join a team");
        player.sendMessage("§f/team leave §7- Leave your current team");
        player.sendMessage("§f/team list §7- List all teams");
    }

    private void listTeams(Player player) {
        player.sendMessage("§6Available Teams:");
        for (String teamName : plugin.getConfig().getConfigurationSection("CONFIG.TEAMS.list").getKeys(false)) {
            String displayName = plugin.getConfig().getString("CONFIG.TEAMS.list." + teamName + ".display-name");
            player.sendMessage("§7- §f" + displayName);
        }
    }
} 