package com.survivorserver.GlobalMarket.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Listing;

public class ListingClickEvent extends Event {

	private InventoryClickEvent event;
	private InterfaceViewer viewer;
	private Listing listing;
	private static final HandlerList handlers = new HandlerList();
	
	public ListingClickEvent(InventoryClickEvent event, InterfaceViewer viewer, Listing listing) {
		this.event = event;
		this.viewer = viewer;
		this.listing = listing;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
        return handlers;
    }
	
	public InventoryClickEvent getClickEvent() {
		return event;
	}
	
	public InterfaceViewer getViewer() {
		return viewer;
	}
	
	public Listing getListing() {
		return listing;
	}
	
	public void setListing(Listing listing) {
		this.listing = listing;
	}
}
