package com.survivorserver.GlobalMarket;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

public class MarketQueue implements Runnable {
	
	Market market;
	MarketStorage storage;
	
	public MarketQueue(Market market, MarketStorage storage) {
		this.market = market;
		this.storage = storage;
		market.getServer().getScheduler().scheduleSyncRepeatingTask(market, this, 0, 1200);
	}
	
	public enum QueueType {
		LISTING_CREATE("listing_create"), MAIL_TO("mail_to");
		
		private String value;
		
		private QueueType(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
	}
	
	public void queueListing(ItemStack item, String seller, double price) {
		storage.storeQueueItem(QueueType.LISTING_CREATE, item, seller, price);
	}
	
	public void queueMail(ItemStack item, String to) {
		storage.storeQueueItem(QueueType.MAIL_TO, item, to);
	}

	@Override
	public void run() {
		try {
			Map<Integer, List<Object>> items = storage.getAllQueueItems();
			if (!items.isEmpty()) {
				for (Entry<Integer, List<Object>> set : items.entrySet()) {
					List<Object> item = set.getValue();
					QueueType type = QueueType.valueOf((String) item.get(0));
					if (type == QueueType.LISTING_CREATE) {
						Long time = (Long) item.get(item.size() - 1);
						if ((System.currentTimeMillis() - time) / 1000 >= market.getTradeTime()) {
							storage.storeListing(((ItemStack) item.get(1)), ((String) item.get(2)), ((Double) item.get(3)));
							storage.removeQueueItem(set.getKey());
						}
					} else if (type == QueueType.MAIL_TO) {
						Long time = (Long) item.get(item.size() - 1);
						if ((System.currentTimeMillis() - time) / 1000 >= market.getMailTime()) {
							storage.storeMail(((ItemStack) item.get(1)), ((String) item.get(2)), true);
						}
					}
				}
			}
		} catch(Exception e) {
			market.log.severe("You shouldn't see this message! Could not process queue:");
			e.printStackTrace();
		}
	}
}
