package com.survivorserver.GlobalMarket;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class Mail implements MarketItem {

	String owner;
	int id;
	ItemStack item;
	double pickup;
	String sender;
	
	public Mail(String owner, int id, ItemStack item, double pickup, String sender) {
		this.owner = owner;
		this.id = id;
		this.item = item;
		this.pickup = pickup;
		this.sender = sender;
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public ItemStack getItem() {
		return item;
	}

	public String getOwner() {
		return owner;
	}
	
	public double getPickup() {
		return pickup;
	}
	
	public String getSender() {
		return sender;
	}
}
