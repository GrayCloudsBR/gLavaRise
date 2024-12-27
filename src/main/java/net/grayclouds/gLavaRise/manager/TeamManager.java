package net.grayclouds.gLavaRise.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.*;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.Component;

public class TeamManager {
    private final Plugin plugin;
    private final Map<String, Team> teams;
    private final Map<UUID, String> playerTeams;
    private final Scoreboard scoreboard;
    private final SoundManager soundManager;
    
    public TeamManager(Plugin plugin) {
        this.plugin = plugin;
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.soundManager = new SoundManager(plugin);
        loadTeamsFromConfig();
    }
    
    private void loadTeamsFromConfig() {
        if (!plugin.getConfig().getBoolean("CONFIG.TEAMS.enabled", false)) return;
        
        for (String teamKey : plugin.getConfig().getConfigurationSection("CONFIG.TEAMS.list").getKeys(false)) {
            String displayName = plugin.getConfig().getString("CONFIG.TEAMS.list." + teamKey + ".display-name", teamKey);
            String colorStr = plugin.getConfig().getString("CONFIG.TEAMS.list." + teamKey + ".color", "WHITE");
            
            createTeam(teamKey, displayName, colorStr);
        }
    }
    
    private void createTeam(String name, String displayName, String colorStr) {
        Team team = scoreboard.registerNewTeam(name);
        team.displayName(Component.text(displayName));
        NamedTextColor color = NamedTextColor.NAMES.value(colorStr.toLowerCase());
        if (color != null) {
            team.color(color);
        }
        team.setAllowFriendlyFire(plugin.getConfig().getBoolean("CONFIG.TEAMS.friendly-fire", false));
        team.setCanSeeFriendlyInvisibles(true);
        teams.put(name, team);
    }
    
    public void addPlayerToTeam(Player player, String teamName) {
        Team team = teams.get(teamName);
        if (team != null) {
            team.addEntry(player.getName());
            playerTeams.put(player.getUniqueId(), teamName);
            player.setScoreboard(scoreboard);
            
            String joinMessage = plugin.getConfig().getString("CONFIG.MESSAGES.team-join", "§aYou joined team %team%!")
                    .replace("%team%", team.displayName().toString());
            player.sendMessage(joinMessage);
            soundManager.playTeamJoinSound(player);
        }
    }
    
    public void removePlayerFromTeam(Player player) {
        String teamName = playerTeams.get(player.getUniqueId());
        if (teamName != null) {
            Team team = teams.get(teamName);
            if (team != null) {
                team.removeEntry(player.getName());
            }
            playerTeams.remove(player.getUniqueId());
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            soundManager.playTeamLeaveSound(player);
        }
    }
    
    public boolean arePlayersInSameTeam(Player player1, Player player2) {
        String team1 = playerTeams.get(player1.getUniqueId());
        String team2 = playerTeams.get(player2.getUniqueId());
        return team1 != null && team1.equals(team2);
    }
    
    public Set<Player> getTeammates(Player player) {
        Set<Player> teammates = new HashSet<>();
        String teamName = playerTeams.get(player.getUniqueId());
        if (teamName != null) {
            Team team = teams.get(teamName);
            if (team != null) {
                for (String entry : team.getEntries()) {
                    Player teammate = Bukkit.getPlayer(entry);
                    if (teammate != null && teammate.isOnline()) {
                        teammates.add(teammate);
                    }
                }
            }
        }
        return teammates;
    }
    
    public void reset() {
        for (Team team : teams.values()) {
            team.unregister();
        }
        teams.clear();
        playerTeams.clear();
        loadTeamsFromConfig();
    }
    
    public boolean isTeamEmpty(String teamName) {
        Team team = teams.get(teamName);
        return team == null || team.getEntries().isEmpty();
    }
    
    public String getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }
    
    public boolean hasTeam(Player player) {
        return playerTeams.containsKey(player.getUniqueId());
    }
    
    public Set<Player> getTeamMembers(String teamName) {
        Set<Player> members = new HashSet<>();
        Team team = teams.get(teamName);
        if (team != null) {
            for (String entry : team.getEntries()) {
                Player player = Bukkit.getPlayer(entry);
                if (player != null && player.isOnline()) {
                    members.add(player);
                }
            }
        }
        return members;
    }
} 