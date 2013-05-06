package com.survivorserver.GlobalMarket;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;
import com.survivorserver.GlobalMarket.InterfaceViewer.ViewType;

public class InterfaceListener implements Listener {

	Market market;
	InterfaceHandler handler;
	MarketStorage storage;
	MarketCore core;
	
	public InterfaceListener(Market market, InterfaceHandler handler, MarketStorage storage, MarketCore core) {
		this.market = market;
		this.handler = handler;
		this.storage = storage;
		this.core = core;
	}
	
	@EventHandler
	public void handleClickEvent(InventoryClickEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
		if (viewer != null) {
			event.setCancelled(true);
			if (event.getSlot() < 45 && !event.isRightClick()) {
				if (viewer.getViewType() == ViewType.MAIL) {
					handleMailAction(event, viewer);
				}
				if (viewer.getViewType() == ViewType.LISTINGS) {
					handleListingsAction(event, viewer);
				}
			} else if (event.isRightClick()) {
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
			} else {
				if (event.getSlot() == 47) {
					if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
						if (viewer.getSearch() == null) {
							Player player = (Player) event.getWhoClicked();
							player.closeInventory();
							handler.removeViewer(viewer);
							player.sendMessage(ChatColor.GREEN + market.getLocale().get("type_your_search"));
							final String name = player.getName();
							market.addSearcher(name);
							market.getServer().getScheduler().scheduleSyncDelayedTask(market, new Runnable() {
								public void run() {
									if (market.searching.contains(name)) {
										market.searching.remove(name);
										Player player = market.getServer().getPlayer(name);
										if (player != null) {
											player.sendMessage(market.prefix + market.getLocale().get("search_cancelled"));
										}
									}
								}
							}, 200);
							return;
						} else {
							viewer.setSearch(null);
							viewer.setLastAction(null);
						}
					}
				}
				if (event.getSlot() == 53) {
					if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
						viewer.setPage(viewer.getPage() + 1);
						viewer.setLastAction(null);
					}
				}
				if (event.getSlot() == 45) {
					if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
						viewer.setPage(viewer.getPage() - 1);
						viewer.setLastAction(null);
					}
				}
			}
			handler.refreshViewer(viewer);
		}
	}
	
	public void handleListingsAction(InventoryClickEvent event, InterfaceViewer viewer) {
		if (viewer.getLastAction() == null) {
			if (event.isShiftClick()) {
				viewer.setLastAction(InterfaceAction.SHIFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
			} else if (event.isLeftClick()) {
				viewer.setLastAction(InterfaceAction.LEFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
			}
		} else if (viewer.getLastActionSlot() == event.getSlot()) {
			if (event.isShiftClick() && viewer.getLastAction() == InterfaceAction.SHIFTCLICK) {
				if (viewer.getBoundSlots().containsKey(event.getSlot())) {
					Listing listing = storage.getListing(viewer.getBoundSlots().get(event.getSlot()));
					if (listing != null) {
						if (viewer.getViewer().equalsIgnoreCase(listing.getSeller()) || handler.isAdmin(viewer.getViewer())) {
							core.removeListing(listing, viewer.getViewer());
						}
					}
				}
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
			} else if (event.isLeftClick() && viewer.getLastAction() == InterfaceAction.LEFTCLICK) {
				if (viewer.getBoundSlots().containsKey(event.getSlot())) {
					Listing listing = storage.getListing(viewer.getBoundSlots().get(event.getSlot()));
					if (listing != null) {
						if (!listing.getSeller().equalsIgnoreCase(event.getWhoClicked().getName())) {
							if (market.getEcon().has(event.getWhoClicked().getName(), listing.price)) {
								core.buyListing(listing, event.getWhoClicked().getName());
							}
						}
					}
				}
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
			} else {
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
			}
		} else {
			viewer.setLastAction(null);
			viewer.setLastActionSlot(-1);
		}
	}
	
	public void handleMailAction(InventoryClickEvent event, InterfaceViewer viewer) {
		if (event.isLeftClick() && viewer.getBoundSlots().containsKey(event.getSlot())) {
			PlayerInventory playerInv = event.getWhoClicked().getInventory();
			if (playerInv.firstEmpty() >= 0) {
				if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() instanceof BookMeta) {
					BookMeta meta = (BookMeta) event.getCurrentItem().getItemMeta();
					if (meta.getTitle().equalsIgnoreCase(market.getLocale().get("transaction_log.item_name"))) {
						double amount = storage.getPaymentAmount(viewer.getBoundSlots().get(event.getSlot()), viewer.getViewer());
						if (amount > 0) {
							market.getEcon().depositPlayer(viewer.getViewer(), amount);
							storage.nullifyPayment(viewer.getBoundSlots().get(event.getSlot()), viewer.getViewer());
							((Player) event.getWhoClicked()).sendMessage(ChatColor.GREEN + market.getLocale().get("picked_up_your_earnings", market.getEcon().format(market.getEcon().getBalance(viewer.getViewer()))));
						}
					}
				}
				core.retrieveMail(viewer.getBoundSlots().get(event.getSlot()), event.getWhoClicked().getName());
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
			} else {
				viewer.setLastAction(InterfaceAction.LEFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
			}
		}
	}
	
	@EventHandler
	public void handleInventoryClose(InventoryCloseEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getPlayer().getName());
		if (viewer != null) {
			handler.removeViewer(viewer);
		}
	}
}
