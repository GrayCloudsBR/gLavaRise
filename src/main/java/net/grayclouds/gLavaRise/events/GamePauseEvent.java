package net.grayclouds.gLavaRise.events;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GamePauseEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final World world;
    private final boolean isPaused;

    public GamePauseEvent(World world, boolean isPaused) {
        this.world = world;
        this.isPaused = isPaused;
    }

    public World getWorld() {
        return world;
    }

    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
} 