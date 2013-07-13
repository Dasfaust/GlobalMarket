package com.survivorserver.GlobalMarket.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketCore;
import com.survivorserver.GlobalMarket.MarketStorage;

public class ExpireTask extends BukkitRunnable {

	Market market;
	MarketStorage storage;
	MarketCore core;
	
	public ExpireTask(Market market, MarketStorage storage, MarketCore core) {
		this.market = market;
		this.storage = storage;
		this.core = core;
	}
	
	@Override
	public void run() {
		for (Listing listing : storage.getListings()) {
			if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
				long diff = System.currentTimeMillis() - listing.getTime() * 1000;
				if ((diff / (60 * 60 * 1000)) >= market.getExpireTime()) {
					core.removeListing(listing, "Server");
				} else {
					break;
				}
			}
		}
	}
}
