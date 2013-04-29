package com.survivorserver.GlobalMarket.tasks;

import com.survivorserver.GlobalMarket.Listing;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketCore;
import com.survivorserver.GlobalMarket.MarketStorage;

public class ExpireTask implements Runnable {

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
		for (Listing listing : storage.getAllListings()) {
			long diff = System.currentTimeMillis() - listing.getTime()*1000;
			if ((diff / (24 * 60 * 60 * 1000)) >= 7) {
				core.removeListing(listing, "Server");
			}
		}
	}
}
