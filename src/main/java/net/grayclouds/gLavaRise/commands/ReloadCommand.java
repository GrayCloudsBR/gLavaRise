package net.grayclouds.gLavaRise.commands;

import net.grayclouds.gLavaRise.manager.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class ReloadCommand implements CommandExecutor {
    private final ConfigManager configManager;
    
    public ReloadCommand(Plugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("glavarise.reload")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        configManager.reload();
        sender.sendMessage("§a[GLavaRise] Configuration reloaded!");
        return true;
    }
} 