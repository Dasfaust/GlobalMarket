package com.survivorserver.GlobalMarket;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;

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
	public void clickEvent(InventoryClickEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
		ItemStack curItem = event.getCurrentItem();
		boolean marketItem = isMarketItem(curItem);
		// Verify we're in a Market interface
		if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {
			// We're clicking a Market item
			if (marketItem) {
				event.setCancelled(true);
				
				int slot = event.getSlot();
				int invSize = viewer.getGui().getContents().length;
				if (event.isRightClick()) {
					// Cancel any existing actions and start over
					viewer.resetActions();
					handler.refreshViewer(viewer);
				} else {
					// We've left clicked or shift clicked
					// Let's update the viewer object with what's happened, so the handler can do stuff with it
					viewer.setLastAction(event.getAction());
					viewer.setLastActionSlot(slot);
					
					MarketInterface inter = handler.getInterface(viewer.getInterface());
					if (viewer.getBoundSlots().containsKey(event.getSlot())) {
						// This item has an ID attached to it
						MarketItem item = inter.getItem(viewer, viewer.getBoundSlots().get(event.getSlot()));
						// Yay, we've got the MarketItem instance. Let's do stuff with it
						viewer.setLastItem(item.getId());
						viewer.incrementClicks();
						
						if (inter.doSingleClickActions()) {
							if (event.isShiftClick()) {
								inter.handleShiftClickAction(viewer, item, event);
							} else {
								inter.handleLeftClickAction(viewer, item, event);
							}
							handler.refreshViewer(viewer);
						} else {
							handler.refreshViewer(viewer);
							if (viewer.getClicks() == 2) {
								if (event.isShiftClick()) {
									inter.handleShiftClickAction(viewer, item, event);
								} else {
									inter.handleLeftClickAction(viewer, item, event);
								}
							}
							handler.refreshViewer(viewer);
						}
					} else {
						inter.onUnboundClick(market, handler, viewer, slot, event, invSize);
						handler.refreshViewer(viewer);
					}
				}
			}
		} else if (marketItem) {
			// Clicking a Market item and has no viewer object, probably not in the interface, destroy it at all costs
			event.setCancelled(true);
			curItem.setType(Material.AIR);
			if (event.getCursor() != null) {
				event.getCursor().setType(Material.AIR);
			}
		}
	}

	public void handleSingleClick(InventoryClickEvent event, InterfaceViewer viewer, MarketInterface gui, MarketItem item) {
		if (event.isShiftClick()) {
			gui.handleShiftClickAction(viewer, item, event);
		} else if (event.isLeftClick()) {
			gui.handleLeftClickAction(viewer, item, event);
		} else {
			viewer.resetActions();
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
					for (MarketInterface gui : handler.getInterfaces()) {
						if (gui.identifyItem(meta)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
