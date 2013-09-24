package com.survivorserver.GlobalMarket;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class MarketCore {

	Market market;
	InterfaceHandler handler;
	MarketStorage storage;
	
	public MarketCore(Market market, InterfaceHandler handler, MarketStorage storage) {
		this.market = market;
		this.handler = handler;
		this.storage = storage;
	}
	
	public boolean buyListing(Listing listing, Player player, boolean removeListing, boolean mailItem, boolean refreshInterface) {
		double originalPrice = listing.getPrice();
		double cutPrice = originalPrice;
		Economy econ = market.getEcon();
		String seller = listing.getSeller();
		String infAccount = market.getInfiniteAccount();
		boolean isInfinite = listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller());
		String buyer = player.getName();
		String friendlyItemName = listing.getItem().getAmount() + " " + market.getItemName(listing.getItem());
		if (market.cutTransactions() && !market.hasCut(player, listing.getSeller())) {
			cutPrice = originalPrice - market.getCut(originalPrice);
		}
		// Make the transaction between buyer and seller
		EconomyResponse response = econ.withdrawPlayer(buyer, originalPrice);
		if (!response.transactionSuccess()) {
			if (response.type == ResponseType.NOT_IMPLEMENTED) {
				market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the withdrawPlayer() function.");
			}
			return false;
		}
		if (isInfinite && infAccount.length() >= 1) {
			// Put the money earned in the infinite seller's account
			response = econ.depositPlayer(infAccount, cutPrice);
			if (!response.transactionSuccess()) {
				if (response.type == ResponseType.NOT_IMPLEMENTED) {
					market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
				}
				return false;
			}
		} else {
			// Direct deposit?
			if (market.autoPayment()) {
				response = econ.depositPlayer(seller, cutPrice);
				if (!response.transactionSuccess()) {
					if (response.type == ResponseType.NOT_IMPLEMENTED) {
						market.log.severe(econ.getName() + " may not be compatible with GlobalMarket. It does not support the depositPlayer() function.");
					}
					return false;
				}
			} else {
				// Send a Transaction Log
				storage.storePayment(listing.getItem(), seller, cutPrice, buyer, true);
			}
			// Seller's stats
			storage.incrementEarned(seller, cutPrice);
			storage.storeHistory(seller, market.getLocale().get("history.item_sold", friendlyItemName, cutPrice));
			// Buyer's stats
			storage.incrementSpent(seller, originalPrice);
			storage.storeHistory(player.getName(), market.getLocale().get("history.item_bought", friendlyItemName, originalPrice));
		}
		// Transfer the item to where it belongs
		if (!isInfinite && removeListing) {
			market.getStorage().removeListing(buyer, listing.getId());
		}
		if (mailItem) {
			if (market.getMailTime() > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				market.getQueue().queueMail(listing.getItem(), buyer, null);
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", market.getMailTime()));
			} else {
				storage.storeMail(listing.getItem(), buyer, null, true);
			}
		}
		// Update viewers
		if (refreshInterface) {
			handler.updateAllViewers();
		}
		return true;
	}
	
	public void removeListing(Listing listing, Player player) {
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			if (market.getMailTime() > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				market.getQueue().queueMail(listing.getItem(), listing.getSeller(), null);
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", market.getMailTime()));
			} else {
				storage.storeMail(listing.getItem(), listing.getSeller(), null, true);
			}
		}
		storage.removeListing(player.getName(), listing.getId());
		handler.updateAllViewers();
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			String itemName = market.getItemName(listing.getItem());
			if (listing.getSeller().equalsIgnoreCase(player.getName())) {
				storage.storeHistory(player.getName(), market.getLocale().get("history.listing_removed", "You", itemName));
			} else {
				storage.storeHistory(listing.getSeller(), market.getLocale().get("history.listing_removed", player.getName(), itemName));
			}
		}
	}
	
	public synchronized void removeListing(Listing listing, String player) {
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			storage.storeMail(listing.getItem(), listing.getSeller(), null, true);
		}
		storage.removeListing(player, listing.getId());
		handler.updateAllViewers();
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			String itemName = market.getItemName(listing.getItem());
			if (listing.getSeller().equalsIgnoreCase(player)) {
				storage.storeHistory(player, market.getLocale().get("history.listing_removed", "You", itemName));
			} else {
				storage.storeHistory(listing.getSeller(), market.getLocale().get("history.listing_removed", player, itemName));
			}
		}
	}
	
	public void retrieveMail(Mail mail, InterfaceViewer viewer, Player player) {
		Inventory playerInv = player.getInventory();
		ItemStack item = storage.getMailItem(viewer.getName(), mail.getId()).getItem();
		playerInv.addItem(item);
		storage.removeMail(viewer.getName(), mail.getId());
	}
	
	public void notifyPlayer(String player, String notification) {
		Player p = market.getServer().getPlayer(player);
		if (p != null) {
			p.playSound(p.getLocation(), Sound.LEVEL_UP, 0.7f, 1);
			
			p.sendMessage(notification);
		}
	}
	
	public void showHistory(Player player) {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		if (meta == null) {
			meta = (BookMeta) market.getServer().getItemFactory().getItemMeta(book.getType());
		}
		meta.setTitle(market.getLocale().get("history.item_name"));
		meta.setAuthor("Server");
		Map<String, Long> history = storage.getHistory(player.getName(), 15);
		List<String> pages = new ArrayList<String>();
		String pagesStr = market.getLocale().get("history.title", player.getName()) + "\n\n" +
						market.getLocale().get("history.total_earned", market.getEcon().format(storage.getEarned(player.getName()))) + "\n" +
						market.getLocale().get("history.total_spent", market.getEcon().format(storage.getSpent(player.getName()))) + "\n" +
						market.getLocale().get("history.actual_amount_made", market.getEcon().format((storage.getEarned(player.getName()) - storage.getSpent(player.getName()))));
		pages.add(pagesStr);
		pages.set(0, pages.get(0).replace("§f", "").replace("§7", "").replace("§6", ""));
		for (Entry<String, Long> set : history.entrySet()) {
			Date date = new Date(set.getValue() * 1000);
			pages.add(set.getKey() + "\n" + market.getLocale().get("history.at_time", date.toString()));
		}
		meta.setPages(pages);
		book.setItemMeta(meta);
		player.getInventory().addItem(book);
	}
}
