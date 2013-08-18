package com.survivorserver.GlobalMarket;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class Listing implements MarketItem {

	int id;
	ItemStack item;
	String seller;
	double price;
	Long time;
	String clientName;
	
	public Listing(Market market, int id, ItemStack item, String seller, double price, Long time) {
		this.id = id;
		this.item = new ItemStack(item);
		this.seller = seller;
		this.price = price;
		this.time = time;
		clientName = market.getItemName(item);
	}
	
	public Listing(Market market, int id, ItemStack item, int amount, String seller, double price, Long time) {
		this.id = id;
		this.item = new ItemStack(item);
		this.seller = seller;
		this.price = price;
		this.time = time;
		clientName = market.getItemName(item);
	}
	
	public int getId() {
		return id;
	}
	
	public ItemStack getItem() {
		return item.clone();
	}
	
	public String getSeller() {
		return seller;
	}
	
	public double getPrice() {
		return price;
	}
	
	public Long getTime() {
		return time;
	}
	
	public String getClientName() {
		return clientName;
	}
}
