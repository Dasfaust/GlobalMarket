package com.survivorserver.GlobalMarket.Events;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Listing;

public class InterfaceCreateEvent extends Event {

	private InterfaceViewer viewer;
	private List<Listing> listings;
	private static final HandlerList handlers = new HandlerList();
	
	public InterfaceCreateEvent(InterfaceViewer viewer, List<Listing> listings) {
		this.viewer = viewer;
		this.listings = listings;
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
	
	public List<Listing> getListings() {
		return listings;
	}
}
