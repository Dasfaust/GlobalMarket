package com.survivorserver.GlobalMarket.Events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ListingCreateEvent extends Event implements Cancellable {

	private ItemStack item;
	private int amount;
	private double price;
	private Player seller;
	private List<String> args;
	private boolean isCancelled;
	private static final HandlerList handlers = new HandlerList();
	
	public ListingCreateEvent(ItemStack item, int amount, double price, Player seller, List<String> args) {
		this.item = item;
		this.amount = amount;
		this.price = price;
		this.seller = seller;
		this.args = args;
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
	
	public ItemStack getItem() {
		return item;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public double getPrice() {
		return price;
	}
	
	public Player getSeller() {
		return seller;
	}
	
	public List<String> getArgs() {
		return args;
	}
}
