package net.grayclouds.gLavaRise.manager;

import org.bukkit.plugin.Plugin;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerManager {
    private final Set<UUID> alivePlayers = new HashSet<>();

    public PlayerManager(Plugin plugin) {
    }

    public void addPlayer(UUID playerId) {
        alivePlayers.add(playerId);
    }

    public void removePlayer(UUID playerId) {
        alivePlayers.remove(playerId);
    }

    public Set<UUID> getAlivePlayers() {
        return new HashSet<>(alivePlayers);
    }

    public int getAlivePlayerCount() {
        return alivePlayers.size();
    }

    public void reset() {
        alivePlayers.clear();
    }
} 