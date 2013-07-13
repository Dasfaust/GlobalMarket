package com.survivorserver.GlobalMarket;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public synchronized void handleClickEvent(InventoryClickEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
		if (event.getInventory().getTitle().equalsIgnoreCase(market.getLocale().get("interface.listings_title"))
				|| event.getInventory().getTitle().equalsIgnoreCase(market.getLocale().get("interface.mail_title"))) {
			event.setCancelled(true);
			if (viewer != null) {
				if (event.getSlot() < 45 && !event.isRightClick()) {
					if (viewer.getViewType() == ViewType.MAIL) {
						if (isMarketItem(event.getCurrentItem())) {
							handleMailAction(event, viewer);
						}
					}
					if (viewer.getViewType() == ViewType.LISTINGS) {
						if (isMarketItem(event.getCurrentItem())) {
							handleListingsAction(event, viewer);
						}
					}
				} else if (event.isRightClick()) {
					viewer.setLastAction(null);
					viewer.setLastActionSlot(-1);
					viewer.setLastListing(null);
				} else {
					if (event.getSlot() == 47) {
						if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
							if (viewer.getSearch() == null) {
								Player player = (Player) event.getWhoClicked();
								player.closeInventory();
								handler.removeViewer(viewer);
								market.startSearch(player);
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
			} else {
				if (isMarketItem(event.getCurrentItem())) {
					event.getInventory().remove(event.getCurrentItem());
					if (event.getCursor() != null) {
						event.getCursor().setType(Material.AIR);
					}
				}
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
			}
		}
	}
	
	public void handleListingsAction(InventoryClickEvent event, InterfaceViewer viewer) {
		Listing listing = storage.getListing(viewer.getBoundSlots().get(event.getSlot()));
		if (viewer.getLastAction() == null) {
			if (event.isShiftClick()) {
				viewer.setLastAction(InterfaceAction.SHIFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
				viewer.setLastListing(listing);
			} else if (event.isLeftClick()) {
				viewer.setLastAction(InterfaceAction.LEFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
				viewer.setLastListing(listing);
			}
		} else if (viewer.getLastActionSlot() == event.getSlot()) {
			if (event.isShiftClick() && viewer.getLastAction() == InterfaceAction.SHIFTCLICK) {
				if (viewer.getBoundSlots().containsKey(event.getSlot())) {
					if (listing != null) {
						if ((viewer.getViewer().equalsIgnoreCase(listing.getSeller()) || 
								handler.isAdmin(viewer.getViewer())) &&
								(viewer.getLastListing() != null && viewer.getLastListing().getId() == listing.getId())) {
							core.removeListing(listing, (Player) event.getWhoClicked());
						}
					}
				}
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
				viewer.setLastListing(null);
			} else if (event.isLeftClick() && viewer.getLastAction() == InterfaceAction.LEFTCLICK) {
				if (viewer.getBoundSlots().containsKey(event.getSlot())) {
					if (listing != null) {
						if (!listing.getSeller().equalsIgnoreCase(event.getWhoClicked().getName()) &&
								(viewer.getLastListing() != null && viewer.getLastListing().getId() == listing.getId())) {
							if (market.getEcon().has(event.getWhoClicked().getName(), listing.price)) {
								core.buyListing(listing, (Player) event.getWhoClicked());
							}
						}
					}
				}
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
				viewer.setLastListing(null);
			} else {
				viewer.setLastAction(null);
				viewer.setLastActionSlot(-1);
				viewer.setLastListing(null);
			}
		} else {
			viewer.setLastAction(null);
			viewer.setLastActionSlot(-1);
			viewer.setLastListing(null);
		}
	}
	
	public void handleMailAction(InventoryClickEvent event, InterfaceViewer viewer) {
		if (event.isLeftClick() && viewer.getBoundSlots().containsKey(event.getSlot())) {
			PlayerInventory playerInv = event.getWhoClicked().getInventory();
			if (playerInv.firstEmpty() >= 0) {
				if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() instanceof BookMeta) {
					BookMeta meta = (BookMeta) event.getCurrentItem().getItemMeta();
					if (meta.hasTitle()) {
						if (meta.getTitle().equalsIgnoreCase(market.getLocale().get("transaction_log.item_name"))) {
							double amount = storage.getPaymentAmount(viewer.getBoundSlots().get(event.getSlot()), viewer.getViewer());
							if (amount > 0) {
								market.getEcon().depositPlayer(viewer.getViewer(), amount);
								storage.nullifyPayment(viewer.getBoundSlots().get(event.getSlot()), viewer.getViewer());
								((Player) event.getWhoClicked()).sendMessage(ChatColor.GREEN + market.getLocale().get("picked_up_your_earnings", market.getEcon().format(market.getEcon().getBalance(viewer.getViewer()))));
							}
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public synchronized void handleInventoryClose(InventoryCloseEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getPlayer().getName());
		if (viewer != null) {
			handler.removeViewer(viewer);
		}
		// Ugly fix for item duping via shift+click and esc. Oh well, we'll have to wait until Bukkit fixes this
		ItemStack[] items = event.getPlayer().getInventory().getContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				if (isMarketItem(items[i])) {
					event.getPlayer().getInventory().remove(items[i]);
				}
			}
		}
		items = event.getPlayer().getInventory().getArmorContents();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null) {
				if (isMarketItem(items[i])) {
					event.getPlayer().getInventory().remove(items[i]);
				}
			}
		}
	}
	
	@EventHandler
	public synchronized void handleItemPickup(ItemSpawnEvent event) {
		// More fixing for item duping
		ItemStack item = event.getEntity().getItemStack();
		if (item != null && isMarketItem(item)) {
			event.getEntity().remove();
		}
	}
	
	public boolean isMarketItem(ItemStack item) {
		if (item != null) {
			if (item.hasItemMeta()) {
				ItemMeta meta = item.getItemMeta();
				if (meta.hasLore()) {
					for (String lore : meta.getLore()) {
						if (lore.contains(market.getLocale().get("price")) || lore.contains(market.getLocale().get("click_to_retrieve"))) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
