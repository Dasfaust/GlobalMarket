package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketItem;

public class Listing implements MarketItem, Comparable<Listing> {

	public int id;
	public int itemId;
	public int amount;
	public String seller;
	public double price;
	String world;
	public Long time;
	private List<Listing> stacked;
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
		this.stacked = new ArrayList<Listing>();
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

	@Override
	public int compareTo(Listing l) {
		if (isStackable(l)) {
			return (int) (time / 1000);
		}
		return 0;
	}
	
	public boolean isStackable(Listing l) {
		if (l.getSeller().equalsIgnoreCase(this.seller) && l.getItemId() == this.itemId && (l.getPrice() / l.getAmount()) == (this.price / this.amount)) {
			return true;
		}
		return false;
	}
	
	public void addSibling(Listing l) {
		if (!stacked.contains(l)) {
			stacked.add(l);
		}
	}
	
	public int countSiblings() {
		stacked.removeAll(Collections.singleton(null));
		return stacked.size();
	}
	
	public List<Listing> getSiblings() {
		return stacked;
	}
	
	public void setSiblings(List<Listing> siblings) {
		stacked.clear();
		stacked.addAll(siblings);
	}
	
	public static class Comparators {
		
		public static Comparator<Listing> PRICE_LOWEST = new Comparator<Listing>() {
			@Override
			public int compare(Listing o1, Listing o2) {
				return (int) (o1.price - o2.price);
			}
		};
		
		public static Comparator<Listing> PRICE_HIGHEST = new Comparator<Listing>() {
			@Override
			public int compare(Listing o1, Listing o2) {
				return (int) (o2.price - o1.price);
			}
		};
		
		public static Comparator<Listing> AMOUNT_HIGHEST = new Comparator<Listing>() {
			@Override
			public int compare(Listing o1, Listing o2) {
				int o1c = o1.getAmount();
				if (o1.countSiblings() > 0) {
					for (Listing l : o1.getSiblings()) {
						o1c += l.getAmount();
					}
				}
				int o2c = o2.getAmount();
				if (o2.countSiblings() > 0) {
					for (Listing l : o2.getSiblings()) {
						o2c += l.getAmount();
					}
				}
				if (o1c > o2c) {
					return -1;
				} else if (o1c < o2c) {
					return 1;
				} else {
					return 0;
				}
			}
		};
	}
}
