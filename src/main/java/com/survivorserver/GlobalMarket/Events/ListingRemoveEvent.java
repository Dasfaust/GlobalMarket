package com.survivorserver.GlobalMarket.Events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ListingRemoveEvent extends Event implements Cancellable {

	private int id;
	private String viewer;
	private boolean isCancelled;
	private static final HandlerList handlers = new HandlerList();
	
	public ListingRemoveEvent(int id, String viewer) {
		this.id = id;
		this.viewer = viewer;
		isCancelled = false;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		isCancelled = cancelled;
	}
	
	public int getId() {
		return id;
	}
	
	public String getViewerName() {
		return viewer;
	}
}
