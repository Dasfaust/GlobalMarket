package com.survivorserver.GlobalMarket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
	
	public void buyListing(Listing listing, Player player) {
		if (market.autoPayment()) {
			double price = listing.getPrice();
			market.getEcon().withdrawPlayer(player.getName(), price);
			if (market.cutTransactions()) {
				price = price - new BigDecimal(market.getCut(price)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();;
			}
			market.getEcon().depositPlayer(listing.getSeller(), price);
			if (market.getMailTime() > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				market.getQueue().queueMail(listing.getItem(), player.getName());
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", market.getMailTime()));
			} else {
				storage.storeMail(listing.getItem(), player.getName(), true);
			}
			storage.removeListing(listing.getId());
			handler.updateAllViewers();
			// TODO: make this pretty
			String itemName = listing.getItem().getType().toString();
			if (!market.useBukkitNames()) {
				net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(listing.getItem().getTypeId());
				if (itemInfo != null) {
					itemName = itemInfo.getName();
				}
			}
			storage.storeHistory(player.getName(), market.getLocale().get("history.item_listed", itemName + "x" + listing.getItem().getAmount(), price));
			storage.storeHistory(listing.getSeller(), market.getLocale().get("history.item_sold", itemName + "x" + listing.getItem().getAmount(), price));
			notifyPlayer(listing.getSeller(), ChatColor.GREEN + market.getLocale().get("you_sold_your_listing", itemName + "x" + listing.getItem().getAmount()));
			storage.incrementEarned(listing.getSeller(), price);
			storage.incrementSpent(player.getName(), price);
			market.getPrices().storePriceInformation(listing.getItem().getTypeId(), listing.getItem().getData().getData(), listing.getItem().getAmount(), price);
		} else {
			market.getEcon().withdrawPlayer(player.getName(), listing.getPrice());
			storage.storePayment(listing.getItem(), listing.getSeller(), listing.getPrice(), true);
			if (market.getMailTime() > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				market.getQueue().queueMail(listing.getItem(), player.getName());
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", market.getMailTime()));
			} else {
				storage.storeMail(listing.getItem(), player.getName(), true);
			}
			storage.removeListing(listing.getId());
			handler.updateAllViewers();
			// TODO: make this pretty
			String itemName = listing.getItem().getType().toString();
			if (!market.useBukkitNames()) {
				net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(listing.getItem().getTypeId());
				if (itemInfo != null) {
					itemName = itemInfo.getName();
				}
			}
			storage.storeHistory(player.getName(), market.getLocale().get("history.item_listed", itemName + "x" + listing.getItem().getAmount(), listing.getPrice()));
			storage.storeHistory(listing.getSeller(), market.getLocale().get("history.item_sold", itemName + "x" + listing.getItem().getAmount(), listing.getPrice()));
			storage.incrementEarned(listing.getSeller(), listing.getPrice() - market.getCut(listing.getPrice()));
			storage.incrementSpent(player.getName(), listing.getPrice());
			market.getPrices().storePriceInformation(listing.getItem().getTypeId(), listing.getItem().getData().getData(), listing.getItem().getAmount(), listing.getPrice());
		}
	}
	
	public void removeListing(Listing listing, Player player) {
		if (market.getMailTime() > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
			market.getQueue().queueMail(listing.getItem(), listing.getSeller());
			player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", market.getMailTime()));
		} else {
			storage.storeMail(listing.getItem(), listing.getSeller(), true);
		}
		storage.removeListing(listing.getId());
		handler.updateAllViewers();
		// TODO: make this pretty
		String itemName = listing.getItem().getType().toString();
		if (!market.useBukkitNames()) {
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(listing.getItem().getTypeId());
			if (itemInfo != null) {
				itemName = itemInfo.getName();
			}
		}
		if (listing.getSeller().equalsIgnoreCase(player.getName())) {
			storage.storeHistory(player.getName(), market.getLocale().get("history.listing_removed", "You", itemName + "x" + listing.getItem().getAmount()));
		} else {
			storage.storeHistory(listing.getSeller(), market.getLocale().get("history.listing_removed", player.getName(), itemName + "x" + listing.getItem().getAmount()));
		}
	}
	
	public synchronized void removeListing(Listing listing, String player) {
		storage.storeMail(listing.getItem(), listing.getSeller(), true);
		storage.removeListing(listing.getId());
		handler.updateAllViewers();
		// TODO: make this pretty
		String itemName = listing.getItem().getType().toString();
		if (!market.useBukkitNames()) {
			net.milkbowl.vault.item.ItemInfo itemInfo = net.milkbowl.vault.item.Items.itemById(listing.getItem().getTypeId());
			if (itemInfo != null) {
				itemName = itemInfo.getName();
			}
		}
		if (listing.getSeller().equalsIgnoreCase(player)) {
			storage.storeHistory(player, market.getLocale().get("history.listing_removed", "You", itemName + "x" + listing.getItem().getAmount()));
		} else {
			storage.storeHistory(listing.getSeller(), market.getLocale().get("history.listing_removed", player, itemName + "x" + listing.getItem().getAmount()));
		}
	}
	
	public void retrieveMail(int id, String player) {
		Inventory playerInv = market.getServer().getPlayer(player).getInventory();
		playerInv.addItem(storage.getMailItem(player, id));
		storage.removeMail(player, id);
	}
	
	public void notifyPlayer(String player, String notification) {
		Player p = market.getServer().getPlayer(player);
		if (p != null) {
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
