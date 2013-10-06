package com.survivorserver.GlobalMarket;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class Listing implements MarketItem {

	public int id;
	public int itemId;
	public int amount;
	public String seller;
	public double price;
	String world;
	public Long time;
	// Legacy
	ItemStack item;
	
	public Listing() {
	}
	
	public Listing(int id, String seller, int itemId, int amount, double price, String world, Long time) {
		this.id = id;
		this.itemId = itemId;
		this.amount = amount;
		this.seller = seller;
		this.price = price;
		this.world = world;
		this.time = time;
	}
	
	/*
	 * Legacy constructor
	 */
	public Listing(int id, ItemStack item, String seller, double price, Long time) {
		this.id = id;
		this.seller = seller;
		this.price = price;
		this.time = time;
		this.item = item;
	}
	
	public int getId() {
		return id;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public String getSeller() {
		return seller;
	}
	
	public double getPrice() {
		return price;
	}
	
	public String getWorld() {
		return world;
	}
	
	public Long getTime() {
		return time;
	}
	
	/**
	 * Should only be used by the legacy importer
	 * @deprecated
	 * @return ItemStack associated with this item
	 */
	public ItemStack getItem() {
		return item;
	}
}
