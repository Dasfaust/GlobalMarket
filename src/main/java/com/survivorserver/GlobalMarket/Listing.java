package com.survivorserver.GlobalMarket;

import org.bukkit.inventory.ItemStack;

public class Listing {

	int id;
	ItemStack item;
	String seller;
	double price;
	Long time;
	String clientName;
	
	public Listing(Market market, int id, ItemStack item, String seller, double price, Long time) {
		this.id = id;
		this.item = item;
		this.seller = seller;
		this.price = price;
		this.time = time;
		// TODO: figure this one out
		clientName = item.getType().toString();
		if (!market.useBukkitNames()) {
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(item.getTypeId());
			if (itemInfo != null) {
				clientName = itemInfo.getName();
			}
		}
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
