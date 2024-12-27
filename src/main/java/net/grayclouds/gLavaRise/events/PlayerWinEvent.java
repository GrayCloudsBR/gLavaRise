package net.grayclouds.gLavaRise.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerWinEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player winner;

    public PlayerWinEvent(Player winner) {
        this.winner = winner;
    }

    public Player getWinner() {
        return winner;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
} 