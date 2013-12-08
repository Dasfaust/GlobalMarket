package com.survivorserver.GlobalMarket.Interface;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.survivorserver.GlobalMarket.InterfaceHandler;
import com.survivorserver.GlobalMarket.InterfaceViewer;
import com.survivorserver.GlobalMarket.Market;

public abstract class MarketInterface {

	public abstract String getName();

	public abstract String getTitle();

	public abstract int getSize();
	
	public abstract boolean enableSearch();
	
	public abstract boolean doSingleClickActions();
	
	public abstract ItemStack prepareItem(MarketItem item, InterfaceViewer viewer, int page, int slot, boolean leftClick, boolean shiftClick);
	
	public abstract void handleLeftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	public abstract void handleShiftClickAction(InterfaceViewer viewer, MarketItem item, InventoryClickEvent event);
	
	public abstract List<MarketItem> getContents(InterfaceViewer viewer);
	
	public abstract List<MarketItem> doSearch(InterfaceViewer viewer, String search);
	
	public abstract MarketItem getItem(InterfaceViewer viewer, int id);
	
	public abstract ItemStack getItemStack(InterfaceViewer viewer, MarketItem item);
	
	public abstract int getTotalNumberOfItems(InterfaceViewer viewer);
	
	public abstract boolean identifyItem(ItemMeta meta);
	
	public abstract void onInterfacePrepare(InterfaceViewer viewer, List<MarketItem> contents, ItemStack[] invContents, Inventory inv);
	
	public void onUnboundClick(Market market, InterfaceHandler handler, InterfaceViewer viewer, int slot, InventoryClickEvent event, int invSize) {
		Player player = (Player) event.getWhoClicked();
		
		// Searching
		if (slot == invSize - 7) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				if (viewer.getSearch() == null) {
					player.closeInventory();
					market.startSearch(player, viewer.getInterface().getName());
					handler.removeViewer(viewer);
					return;
				} else {
					// Cancel search
					viewer.setSearch(null);
					viewer.resetActions();
					handler.refreshViewer(viewer, viewer.getInterface().getName());
				}
			}
		}
		
		// Next page
		if (slot == invSize - 1) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				viewer.setPage(viewer.getPage() + 1);
				viewer.resetActions();
				handler.refreshViewer(viewer, viewer.getInterface().getName());
			}
		}
		
		// Previous page
		if (slot == invSize - 9) {
			if (event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
				viewer.setPage(viewer.getPage() - 1);
				viewer.resetActions();
				handler.refreshViewer(viewer, viewer.getInterface().getName());
			}
		}
	}
}
