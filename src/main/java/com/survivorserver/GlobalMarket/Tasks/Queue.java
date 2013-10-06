package com.survivorserver.GlobalMarket.Tasks;

import java.util.Collection;

import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.survivorserver.GlobalMarket.Market;
import com.survivorserver.GlobalMarket.MarketStorage;
import com.survivorserver.GlobalMarket.QueueItem;

public class Queue extends BukkitRunnable {

	Market market;
	MarketStorage storage;
	
	public Queue(Market market) {
		this.market = market;
		storage = market.getStorage();
	}
	
	@Override
	public void run() {
		Collection<QueueItem> expired = Collections2.filter(storage.getQueue(), new Predicate<QueueItem>() {
			@Override
			public boolean apply(QueueItem item) {
				if (item.getMail() != null) {
					if (((System.currentTimeMillis() - item.getTime()) / 1000) / 60 >= market.getMailTime()) {
						return true;
					}
				} else {
					if (((System.currentTimeMillis() - item.getTime()) / 1000) / 60 >= market.getTradeTime()
							&& !item.getListing().getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
						return true;
					}
				}
				return false;
			}
		});
		for (QueueItem item : expired) {
			storage.removeItemFromQueue(item.getId());
		}
	}
}
