package com.survivorserver.GlobalMarket;

import org.bukkit.Material;
import org.bukkit.entity.Player;
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
import com.survivorserver.GlobalMarket.InterfaceViewer.InterfaceAction;

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
		if (viewer != null && event.getInventory().getName().equalsIgnoreCase(viewer.getGui().getName())) {
			event.setCancelled(true);
			if (viewer != null) {
				if ((event.getSlot() < viewer.getGui().getContents().length - 9 && !event.isRightClick())
						&& (event.isLeftClick() && viewer.getBoundSlots().containsKey(event.getSlot()))) {
					MarketInterface gui = handler.getInterface(viewer.getInterface());
					MarketItem item = gui.getItem(viewer, viewer.getBoundSlots().get(event.getSlot()));
					if (gui.doSingleClickActions()) {
						handleSingleClick(event, viewer, gui, item);
					} else {
						handleDoubleClick(event, viewer, gui, item);
					}
				} else if (event.isRightClick()) {
					viewer.setLastAction(null);
					viewer.setLastActionSlot(-1);
					viewer.setLastItem(null);
				} else {
					if (event.getSlot() == viewer.getGui().getContents().length - 7) {
						if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
							if (viewer.getSearch() == null) {
								Player player = (Player) event.getWhoClicked();
								player.closeInventory();
								market.startSearch(player, viewer.getInterface());
								handler.removeViewer(viewer);
								return;
							} else {
								viewer.setSearch(null);
								viewer.setLastAction(null);
							}
						}
					}
					if (event.getSlot() == viewer.getGui().getContents().length - 1) {
						if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
							viewer.setPage(viewer.getPage() + 1);
							viewer.setLastAction(null);
						}
					}
					if (event.getSlot() == viewer.getGui().getContents().length - 9) {
						if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
							viewer.setPage(viewer.getPage() - 1);
							viewer.setLastAction(null);
						}
					}
				}
				handler.refreshViewer(viewer);
			}
		} else {
			if (isMarketItem(event.getCurrentItem())) {
				event.getInventory().remove(event.getCurrentItem());
				if (event.getCursor() != null) {
					event.getCursor().setType(Material.AIR);
				}
				event.getWhoClicked().closeInventory();
				event.setCancelled(true);
			}
		}
	}
	
	private void handleDoubleClick(InventoryClickEvent event, InterfaceViewer viewer, MarketInterface gui, MarketItem item) {
		if (viewer.getLastAction() == null) {
			if (event.isShiftClick()) {
				viewer.setLastAction(InterfaceAction.SHIFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
				viewer.setLastItem(item);
			} else if (event.isLeftClick()) {
				viewer.setLastAction(InterfaceAction.LEFTCLICK);
				viewer.setLastActionSlot(event.getSlot());
				viewer.setLastItem(item);
			}
		} else {
			if (event.isShiftClick() && viewer.getLastAction() == InterfaceAction.SHIFTCLICK) {
				gui.handleShiftClickAction(viewer, item, event);
			} else if (event.isLeftClick() && viewer.getLastAction() == InterfaceAction.LEFTCLICK) {
				gui.handleLeftClickAction(viewer, item, event);
			} else {
				viewer.resetActions();
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
