package net.grayclouds.gLavaRise.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GameEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final World world;

    public GameEndEvent(World world) {
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 