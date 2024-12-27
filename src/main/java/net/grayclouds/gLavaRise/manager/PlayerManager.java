package net.grayclouds.gLavaRise.manager;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import net.grayclouds.gLavaRise.events.PlayerEliminationEvent;
import net.grayclouds.gLavaRise.events.GameEndEvent;
import net.grayclouds.gLavaRise.GLavaRise;

public class PlayerManager {
    private final Plugin plugin;
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();

    public PlayerManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addPlayer(Player player) {
        alivePlayers.add(player.getUniqueId());
        player.setGameMode(GameMode.SURVIVAL);
    }

    public void eliminatePlayer(Player player) {
        if (!isPlayerAlive(player)) return;
        
        alivePlayers.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());
        player.setGameMode(GameMode.SPECTATOR);
        
        // Call elimination event
        plugin.getServer().getPluginManager().callEvent(new PlayerEliminationEvent(player));
        
        // Check for win condition
        if (alivePlayers.size() == 1) {
            Player winner = plugin.getServer().getPlayer(alivePlayers.iterator().next());
            if (winner != null) {
                plugin.getServer().getPluginManager().callEvent(new GameEndEvent(winner.getWorld()));
            }
        }
        
        // Handle team elimination
        String teamName = ((GLavaRise)plugin).getTeamManager().getPlayerTeam(player);
        if (teamName != null) {
            ((GLavaRise)plugin).getTeamManager().removePlayerFromTeam(player);
            
            // Check if team is eliminated
            if (((GLavaRise)plugin).getTeamManager().getTeamMembers(teamName).isEmpty()) {
                String teamEliminatedMsg = plugin.getConfig().getString("CONFIG.MESSAGES.team-eliminated", "§cTeam %team% has been eliminated!")
                    .replace("%team%", teamName);
                plugin.getServer().broadcast(net.kyori.adventure.text.Component.text(teamEliminatedMsg));
            }
        }
    }

    public boolean isPlayerAlive(Player player) {
        return alivePlayers.contains(player.getUniqueId());
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player.getUniqueId());
    }

    public void reset() {
        alivePlayers.clear();
        spectators.clear();
    }

    public int getAlivePlayerCount() {
        return alivePlayers.size();
    }

    public Set<UUID> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    public void giveStartingItems(Player player) {
        if (!plugin.getConfig().getBoolean("CONFIG.GAME.starting-items.enabled", true)) {
            return;
        }

        List<Map<?, ?>> items = plugin.getConfig().getMapList("CONFIG.GAME.starting-items.items");
        for (Map<?, ?> item : items) {
            try {
                String materialName = (String) item.get("material");
                int amount = item.containsKey("amount") ? ((Number) item.get("amount")).intValue() : 1;
                
                Material material = Material.valueOf(materialName.toUpperCase());
                ItemStack itemStack = new ItemStack(material, amount);
                player.getInventory().addItem(itemStack);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid starting item configuration: " + item);
            }
        }
    }
} 