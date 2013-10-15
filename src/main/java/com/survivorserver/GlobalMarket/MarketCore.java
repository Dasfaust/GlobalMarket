package com.survivorserver.GlobalMarket;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.survivorserver.GlobalMarket.HistoryHandler.MarketAction;

public class MarketCore {

	Market market;
	InterfaceHandler handler;
	MarketStorage storage;
	
	public MarketCore(Market market, InterfaceHandler handler, MarketStorage storage) {
		this.market = market;
		this.handler = handler;
		this.storage = storage;
	}
	
	public boolean buyListing(Listing listing, Player player, InterfaceViewer viewer, boolean removeListing, boolean mailItem, boolean refreshInterface) {
		double originalPrice = listing.getPrice();
		double cutPrice = originalPrice;
		Economy econ = market.getEcon();
		String seller = listing.getSeller();
		String infAccount = market.getInfiniteAccount();
		boolean isInfinite = listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller());
		String buyer = player.getName();
		double cut = market.getCut(listing.getPrice(), listing.getSeller(), listing.getWorld());
		if (cut > 0) {
			cutPrice = originalPrice - cut;
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
				ItemStack item = viewer.getInterface().getItemStack(viewer, listing);
				storage.storePayment(item, seller, buyer, cutPrice, listing.getWorld());
			}
			// Seller's stats
			if (market.enableHistory()) {
				market.getHistory().storeHistory(seller, buyer, MarketAction.LISTING_SOLD, listing.getItemId(), listing.getAmount(), originalPrice);
				market.getHistory().incrementEarned(seller, cutPrice);
				market.getHistory().incrementSpent(buyer, originalPrice);
				market.getHistory().storeHistory(buyer, seller, MarketAction.LISTING_BOUGHT, listing.getItemId(), listing.getAmount(), originalPrice);
			}
		}
		// Transfer the item to where it belongs
		if (mailItem) {
			int mailTime = market.getMailTime(player);
			if (mailTime > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				storage.queueMail(buyer, null, listing.getItemId(), listing.getAmount(), listing.getWorld());
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", mailTime));
			} else {
				storage.createMail(buyer, null, listing.getItemId(), listing.getAmount(), listing.getWorld());
			}
		}
		if (!isInfinite && removeListing) {
			storage.removeListing(listing.getId());
		}
		// Update viewers
		if (refreshInterface) {
			handler.updateAllViewers();
		}
		return true;
	}
	
	public void removeListing(Listing listing, Player player) {
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			int mailTime = market.getMailTime(player);
			if (mailTime > 0 && market.queueOnBuy() && !player.hasPermission("globalmarket.noqueue")) {
				storage.queueMail(listing.getSeller(), null, listing.getItemId(), listing.getAmount(), listing.getWorld());
				player.sendMessage(ChatColor.GREEN + market.getLocale().get("item_will_send", mailTime));
			} else {
				storage.createMail(listing.getSeller(), null, listing.getItemId(), listing.getAmount(), listing.getWorld());
			}
		}
		storage.removeListing(listing.getId());
		handler.updateAllViewers();
		if (market.enableHistory()) {
			if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
				if (listing.getSeller().equalsIgnoreCase(player.getName())) {;
					market.getHistory().storeHistory(listing.getSeller(), "You", MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
				} else {
					market.getHistory().storeHistory(listing.getSeller(), player.getName(), MarketAction.LISTING_REMOVED, listing.getItemId(), listing.getAmount(), 0);
				}
			}
		}
	}
	
	public synchronized void expireListing(Listing listing) {
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			storage.createMail(listing.getSeller(), "Expired", listing.getItemId(), listing.getAmount(), listing.getWorld());
		}
		storage.removeListing(listing.getId());
		handler.updateAllViewers();
		if (!listing.getSeller().equalsIgnoreCase(market.getInfiniteSeller())) {
			if (market.enableHistory()) {
				market.getHistory().storeHistory(listing.getSeller(), null, MarketAction.LISTING_EXPIRED, listing.getItemId(), listing.getAmount(), 0);
			}
		}
	}
	
	public void retrieveMail(Mail mail, InterfaceViewer viewer, Player player) {
		Inventory playerInv = player.getInventory();
		ItemStack item = storage.getItem(mail.getItemId(), mail.getAmount());
		playerInv.addItem(item);
		storage.removeMail(mail.getId());
	}
	
	public void notifyPlayer(String player, String notification) {
		Player p = market.getServer().getPlayer(player);
		if (p != null) {
			p.sendMessage(notification);
		}
	}
	
	/*public void showHistory(Player player) {
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
		pages.set(0, pages.get(0).replace("�f", "").replace("�7", "").replace("�6", ""));
		for (Entry<String, Long> set : history.entrySet()) {
			Date date = new Date(set.getValue() * 1000);
			pages.add(set.getKey() + "\n" + market.getLocale().get("history.at_time", date.toString()));
		}
		meta.setPages(pages);
		book.setItemMeta(meta);
		player.getInventory().addItem(book);
	}*/
}
