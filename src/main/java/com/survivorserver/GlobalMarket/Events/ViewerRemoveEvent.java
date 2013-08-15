package com.survivorserver.GlobalMarket.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.survivorserver.GlobalMarket.InterfaceViewer;

public class ViewerRemoveEvent extends Event {

	private InterfaceViewer viewer;
	private static final HandlerList handlers = new HandlerList();
	
	public ViewerRemoveEvent(InterfaceViewer viewer) {
		this.viewer = viewer;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public InterfaceViewer getViewer() {
		return viewer;
	}
}
