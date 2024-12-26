package net.grayclouds.gLavaRise.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import net.grayclouds.gLavaRise.handler.BorderDamageHandler;

public class ReloadCommand implements CommandExecutor {
    private final Plugin plugin;
    
    public ReloadCommand(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("glavarise.reload")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        
        plugin.reloadConfig();
        BorderDamageHandler.reload();
        sender.sendMessage("§a[GLavaRise] Configuration reloaded!");
        return true;
    }
} 