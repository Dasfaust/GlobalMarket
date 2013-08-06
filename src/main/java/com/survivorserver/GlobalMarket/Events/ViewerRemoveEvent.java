package com.survivorserver.GlobalMarket.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ViewerRemoveEvent extends Event {

	private String viewer;
	private static final HandlerList handlers = new HandlerList();
	
	public ViewerRemoveEvent(String viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public String getViewerName() {
		return viewer;
	}
}
