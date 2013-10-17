package com.survivorserver.GlobalMarket;

import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import com.survivorserver.GlobalMarket.Interface.MarketInterface;
import com.survivorserver.GlobalMarket.Interface.MarketItem;
import com.survivorserver.GlobalMarket.Lib.NbtFactory;
import com.survivorserver.GlobalMarket.Lib.NbtFactory.NbtCompound;

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
		//ItemStack curItem = event.getCurrentItem();
		int rawSlot = event.getRawSlot();
		int slot = event.getSlot();
		// Verify we're in a Market interface
		if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {
			//int guiSize = handler.getInterface(viewer.getInterface()).getSize() - 1;
			if (rawSlot <= 53) {
				// We've clicked a Market item
				event.setResult(Result.DENY);
				
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
					
					MarketInterface inter = viewer.getInterface();
					if (viewer.getBoundSlots().containsKey(rawSlot)) {
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
						} else {
							handler.refreshViewer(viewer);
							if (viewer.getClicks() == 2) {
								if (event.isShiftClick()) {
									inter.handleShiftClickAction(viewer, item, event);
								} else {
									inter.handleLeftClickAction(viewer, item, event);
								}
							}
						}
					} else {
						inter.onUnboundClick(market, handler, viewer, slot, event, invSize);
						handler.refreshViewer(viewer);
					}
				}
			} else if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY
					|| (event.getAction() == InventoryAction.PLACE_ALL
					|| event.getAction() == InventoryAction.PLACE_ONE
					|| event.getAction() == InventoryAction.PLACE_SOME
					|| event.getAction() == InventoryAction.SWAP_WITH_CURSOR)
					&& event.getRawSlot() == event.getSlot()) {
				// They're trying to put an item from their inventory into the Market inventory. Not bad for us, but they will lose their item. Cancel it because we're nice :)
				event.setResult(Result.DENY);
			}
		}
	}
	
	@EventHandler
	public void handleDrag(InventoryDragEvent event) {
		InterfaceViewer viewer = handler.findViewer(event.getWhoClicked().getName());
		if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {
			//int guiSize = handler.getInterface(viewer.getInterface()).getSize() - 1;
			for (int raw : event.getRawSlots()) {
				if (raw <= 53) {
					event.setCancelled(true);
				}
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
			viewer.getGui().clear();
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
			if (item.getType() != Material.AIR) {
				NbtCompound comp = NbtFactory.fromItemTag(item.clone());
				return comp == null ? false : comp.containsKey("marketItem");
			}
		}
		return false;
	}
}
